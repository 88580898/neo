package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CurrencyCatalog
import com.example.data.local.AppDatabase
import com.example.data.local.BoardCurrencyEntity
import com.example.data.local.ExchangeRateEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.RateProvider
import com.example.data.local.UserPreferences
import com.example.data.remote.NetworkMonitor
import com.example.data.remote.PublicRatesApi
import com.example.data.repository.CurrencyRepository
import com.example.data.repository.SyncResult
import com.example.domain.ExpressionEvaluator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiEvent {
    data class ShowMessage(val message: String) : UiEvent
    data class CopyToClipboard(val label: String, val text: String, val toastMessage: String) : UiEvent
}

private data class LocalInteractionState(
    val inputExpression: String = "100",
    val isKeypadVisible: Boolean = true,
    val syncStatus: SyncStatus = SyncStatus.Idle,
    val offlineBaseCode: String = "CNY",
    val offlineSearchQuery: String = "",
    val isSeeded: Boolean = false
)

class CurrencyViewModel(
    private val repository: CurrencyRepository
) : ViewModel() {

    private val localState = MutableStateFlow(LocalInteractionState())
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<CurrencyUiState> = combine(
        repository.allRatesFlow,
        repository.boardCurrenciesFlow,
        repository.preferencesFlow,
        repository.isOnlineFlow,
        localState
    ) { rates, board, prefs, isOnline, local ->
        buildUiState(rates, board, prefs, isOnline, local)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CurrencyUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            localState.update { it.copy(isSeeded = true) }

            val prefs = repository.preferencesFlow.first()
            localState.update { it.copy(offlineBaseCode = prefs.activeCurrencyCode) }

            if (prefs.autoUpdateOnLaunch && !prefs.pureOfflineMode) {
                syncRates(silentFailure = true)
            }
        }
    }

    private fun buildUiState(
        rates: List<ExchangeRateEntity>,
        board: List<BoardCurrencyEntity>,
        prefs: UserPreferences,
        isOnline: Boolean,
        local: LocalInteractionState
    ): CurrencyUiState {
        val ratesByCode = rates.associateBy { it.code }
        val boardCodes = if (board.isNotEmpty()) {
            board.map { it.code }
        } else {
            CurrencyCatalog.defaultBoardCodes
        }
        val activeCode = if (prefs.activeCurrencyCode in boardCodes) {
            prefs.activeCurrencyCode
        } else {
            boardCodes.firstOrNull() ?: "CNY"
        }

        val activeRateFromUsd = ratesByCode[activeCode]?.effectiveRateFromUsd
            ?: CurrencyCatalog.getOrFallback(activeCode).defaultRateFromUsd

        val evaluatedInput = ExpressionEvaluator.evaluate(local.inputExpression)
        val hasOperator = ExpressionEvaluator.hasOperator(local.inputExpression)

        val boardItems = boardCodes.map { code ->
            val info = CurrencyCatalog.getOrFallback(code)
            val entity = ratesByCode[code]
            val targetFromUsd = entity?.effectiveRateFromUsd ?: info.defaultRateFromUsd
            val isActive = code == activeCode

            val unitRate = if (activeRateFromUsd > 0.0) targetFromUsd / activeRateFromUsd else 1.0
            val converted = if (isActive) evaluatedInput else evaluatedInput * unitRate

            val formattedAmount = if (isActive) {
                if (hasOperator) {
                    ExpressionEvaluator.formatConvertedAmount(evaluatedInput, info.fractionDigits)
                } else {
                    ExpressionEvaluator.formatDisplayExpression(local.inputExpression)
                }
            } else {
                ExpressionEvaluator.formatConvertedAmount(converted, info.fractionDigits)
            }

            val unitRateText = if (isActive) {
                "基准货币 · 1 ${info.code}"
            } else {
                "1 $activeCode = ${ExpressionEvaluator.formatUnitRate(unitRate)} ${info.code}"
            }

            BoardCurrencyItem(
                info = info,
                isActive = isActive,
                formattedAmount = formattedAmount,
                rawAmount = converted,
                unitRateText = unitRateText,
                isCustomCalibrated = entity?.isCustomCalibrated == true
            )
        }

        val offlineBase = local.offlineBaseCode
        val offlineBaseEntity = ratesByCode[offlineBase]
        val offlineBaseEffective = offlineBaseEntity?.effectiveRateFromUsd
            ?: CurrencyCatalog.getOrFallback(offlineBase).defaultRateFromUsd
        val offlineBasePublic = offlineBaseEntity?.rateFromUsd
            ?: CurrencyCatalog.getOrFallback(offlineBase).defaultRateFromUsd

        val pinnedSet = boardCodes.toSet()
        val query = local.offlineSearchQuery.trim()

        val allCodesOrdered = buildList {
            // Popular catalog currencies first, then remaining catalog, then any extra API codes
            CurrencyCatalog.currencies.forEach { add(it.code) }
            rates.map { it.code }.filterNot { it in this }.sorted().forEach { add(it) }
        }

        val allRateRows = allCodesOrdered.mapNotNull { code ->
            val entity = ratesByCode[code] ?: return@mapNotNull null
            val info = CurrencyCatalog.getOrFallback(code)
            if (!CurrencyCatalog.matchesQuery(info, query)) return@mapNotNull null

            val effectivePerBase = if (offlineBaseEffective > 0.0) {
                entity.effectiveRateFromUsd / offlineBaseEffective
            } else {
                entity.effectiveRateFromUsd
            }
            val publicPerBase = if (offlineBasePublic > 0.0) {
                entity.rateFromUsd / offlineBasePublic
            } else {
                entity.rateFromUsd
            }

            OfflineRateRow(
                info = info,
                effectiveRatePerBase = effectivePerBase,
                publicRatePerBase = publicPerBase,
                formattedEffectiveRate = ExpressionEvaluator.formatUnitRate(effectivePerBase),
                formattedPublicRate = ExpressionEvaluator.formatUnitRate(publicPerBase),
                isCustomCalibrated = entity.isCustomCalibrated,
                isPinnedToBoard = code in pinnedSet,
                source = entity.source
            )
        }

        return CurrencyUiState(
            boardItems = boardItems,
            allRates = allRateRows,
            activeCurrencyCode = activeCode,
            inputExpression = local.inputExpression,
            formattedExpressionPreview = if (hasOperator) {
                ExpressionEvaluator.formatDisplayExpression(local.inputExpression)
            } else {
                ""
            },
            hasOperatorInExpression = hasOperator,
            isKeypadVisible = local.isKeypadVisible,
            isOnline = isOnline,
            pureOfflineMode = prefs.pureOfflineMode,
            autoUpdateOnLaunch = prefs.autoUpdateOnLaunch,
            preferredProvider = prefs.preferredProvider,
            lastSyncMillis = prefs.lastSyncMillis,
            lastSyncSource = prefs.lastSyncSource,
            cachedCurrencyCount = rates.size,
            customCalibratedCount = rates.count { it.isCustomCalibrated },
            syncStatus = local.syncStatus,
            offlineBaseCurrencyCode = offlineBase,
            offlineSearchQuery = local.offlineSearchQuery,
            isLoadingInitial = !local.isSeeded
        )
    }

    // --- Converter Input Actions ---

    fun onDigitPressed(digit: String) {
        localState.update { state ->
            state.copy(inputExpression = ExpressionEvaluator.appendDigit(state.inputExpression, digit))
        }
    }

    fun onDecimalPressed() {
        localState.update { state ->
            state.copy(inputExpression = ExpressionEvaluator.appendDecimal(state.inputExpression))
        }
    }

    fun onOperatorPressed(op: Char) {
        localState.update { state ->
            state.copy(inputExpression = ExpressionEvaluator.appendOperator(state.inputExpression, op))
        }
    }

    fun onBackspacePressed() {
        localState.update { state ->
            state.copy(inputExpression = ExpressionEvaluator.backspace(state.inputExpression))
        }
    }

    fun onClearPressed() {
        localState.update { it.copy(inputExpression = "0") }
    }

    fun onEqualsPressed() {
        localState.update { state ->
            val evaluated = ExpressionEvaluator.evaluate(state.inputExpression)
            state.copy(inputExpression = ExpressionEvaluator.formatEditableResult(evaluated))
        }
    }

    fun onQuickAmountSelected(amount: String) {
        localState.update { it.copy(inputExpression = amount, isKeypadVisible = true) }
    }

    fun toggleKeypadVisible() {
        localState.update { it.copy(isKeypadVisible = !it.isKeypadVisible) }
    }

    fun selectActiveCurrency(code: String) {
        val currentState = uiState.value
        if (code == currentState.activeCurrencyCode) {
            localState.update { it.copy(isKeypadVisible = true) }
            return
        }
        // Carry over that currency's current converted value so switching active currency feels seamless
        val targetItem = currentState.boardItems.firstOrNull { it.info.code == code }
        val carryOverExpression = if (targetItem != null && targetItem.rawAmount > 0.0) {
            ExpressionEvaluator.formatEditableResult(targetItem.rawAmount)
        } else {
            currentState.inputExpression
        }
        localState.update {
            it.copy(
                inputExpression = carryOverExpression,
                isKeypadVisible = true
            )
        }
        viewModelScope.launch {
            repository.setActiveCurrencyCode(code)
        }
    }

    fun replaceBoardCurrency(oldCode: String, newCode: String) {
        viewModelScope.launch {
            val wasActive = uiState.value.activeCurrencyCode == oldCode
            repository.replaceBoardCurrency(oldCode, newCode)
            if (wasActive) {
                repository.setActiveCurrencyCode(newCode)
            }
        }
    }

    fun addCurrencyToBoard(code: String) {
        viewModelScope.launch {
            repository.addToBoard(code)
            val info = CurrencyCatalog.getOrFallback(code)
            _events.send(UiEvent.ShowMessage("已将 ${info.nameZh} (${info.code}) 加入换算面板"))
        }
    }

    fun removeCurrencyFromBoard(code: String) {
        viewModelScope.launch {
            val state = uiState.value
            if (state.boardItems.size <= 2) {
                _events.send(UiEvent.ShowMessage("换算面板至少保留 2 种货币"))
                return@launch
            }
            if (state.activeCurrencyCode == code) {
                val fallback = state.boardItems.firstOrNull { it.info.code != code }?.info?.code ?: "CNY"
                repository.setActiveCurrencyCode(fallback)
            }
            repository.removeFromBoard(code)
        }
    }

    fun copyConvertedValue(item: BoardCurrencyItem) {
        viewModelScope.launch {
            val copyText = "${item.info.symbol}${item.formattedAmount} ${item.info.code}"
            _events.send(
                UiEvent.CopyToClipboard(
                    label = item.info.code,
                    text = item.formattedAmount.replace(",", ""),
                    toastMessage = "已复制 $copyText"
                )
            )
        }
    }

    // --- Public Rate Sync & Offline Management ---

    fun syncRates(silentFailure: Boolean = false) {
        val state = uiState.value
        if (state.syncStatus is SyncStatus.Syncing) return

        viewModelScope.launch {
            if (state.pureOfflineMode) {
                if (!silentFailure) {
                    _events.send(UiEvent.ShowMessage("当前处于纯离线模式，请先关闭离线开关后再联网更新"))
                }
                return@launch
            }

            localState.update { it.copy(syncStatus = SyncStatus.Syncing) }
            when (val result = repository.syncPublicRates(state.preferredProvider, state.pureOfflineMode)) {
                is SyncResult.Success -> {
                    localState.update {
                        it.copy(syncStatus = SyncStatus.Success(result.updatedCount, result.sourceLabel))
                    }
                    if (!silentFailure) {
                        _events.send(
                            UiEvent.ShowMessage("已更新 ${result.updatedCount} 种货币汇率（${result.sourceLabel}）")
                        )
                    }
                }
                is SyncResult.BlockedByOfflineMode -> {
                    localState.update { it.copy(syncStatus = SyncStatus.Idle) }
                    if (!silentFailure) {
                        _events.send(UiEvent.ShowMessage("当前处于纯离线模式，未发起网络请求"))
                    }
                }
                is SyncResult.Failure -> {
                    localState.update { it.copy(syncStatus = SyncStatus.Error(result.reason)) }
                    if (!silentFailure) {
                        _events.send(UiEvent.ShowMessage("网络不可用，继续使用本地离线汇率"))
                    }
                }
            }
        }
    }

    fun setPureOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setPureOfflineMode(enabled)
            _events.send(
                UiEvent.ShowMessage(
                    if (enabled) "已开启纯离线模式，仅使用本地缓存汇率" else "已恢复联网模式，可随时更新公开汇率"
                )
            )
        }
    }

    fun setAutoUpdateOnLaunch(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoUpdateOnLaunch(enabled)
        }
    }

    fun setPreferredProvider(provider: RateProvider) {
        viewModelScope.launch {
            repository.setPreferredProvider(provider)
        }
    }

    fun setOfflineBaseCurrency(code: String) {
        localState.update { it.copy(offlineBaseCode = code.uppercase()) }
    }

    fun setOfflineSearchQuery(query: String) {
        localState.update { it.copy(offlineSearchQuery = query) }
    }

    fun saveCustomOfflineRate(targetCode: String, customRatePerBase: Double) {
        val baseCode = uiState.value.offlineBaseCurrencyCode
        viewModelScope.launch {
            repository.setCustomOfflineRate(targetCode, baseCode, customRatePerBase)
            val info = CurrencyCatalog.getOrFallback(targetCode)
            _events.send(UiEvent.ShowMessage("已保存 ${info.nameZh} (${info.code}) 自定义离线汇率"))
        }
    }

    fun resetCustomOfflineRate(targetCode: String) {
        viewModelScope.launch {
            repository.resetCustomOfflineRate(targetCode)
            val info = CurrencyCatalog.getOrFallback(targetCode)
            _events.send(UiEvent.ShowMessage("已恢复 ${info.nameZh} (${info.code}) 为公开参考汇率"))
        }
    }

    fun exportOfflinePack() {
        viewModelScope.launch {
            val source = uiState.value.lastSyncSource
            val json = repository.exportOfflinePackJson(source)
            _events.send(
                UiEvent.CopyToClipboard(
                    label = "极简汇率离线包",
                    text = json,
                    toastMessage = "已复制离线汇率包到剪贴板，可发送至无网设备导入"
                )
            )
        }
    }

    fun importOfflinePack(rawJson: String) {
        viewModelScope.launch {
            val result = repository.importOfflinePackJson(rawJson)
            result.fold(
                onSuccess = { count ->
                    _events.send(UiEvent.ShowMessage("已成功离线导入 $count 种货币汇率"))
                },
                onFailure = {
                    _events.send(UiEvent.ShowMessage("离线汇率包格式无效，请粘贴完整 JSON 内容"))
                }
            )
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appContext = context.applicationContext
            val db = AppDatabase.getInstance(appContext)
            val moshi = PublicRatesApi.createMoshi()
            val api = PublicRatesApi.create(moshi)
            val prefs = PreferencesManager(appContext)
            val monitor = NetworkMonitor(appContext)
            val repo = CurrencyRepository(
                dao = db.exchangeRateDao(),
                api = api,
                moshi = moshi,
                preferencesManager = prefs,
                networkMonitor = monitor
            )
            return CurrencyViewModel(repo) as T
        }
    }
}
