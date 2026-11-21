package com.example.data.repository

import com.example.data.CurrencyCatalog
import com.example.data.local.BoardCurrencyEntity
import com.example.data.local.ExchangeRateDao
import com.example.data.local.ExchangeRateEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.RateProvider
import com.example.data.local.UserPreferences
import com.example.data.remote.NetworkMonitor
import com.example.data.remote.OfflineRatePack
import com.example.data.remote.PublicRatesApi
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

sealed interface SyncResult {
    data class Success(val updatedCount: Int, val sourceLabel: String) : SyncResult
    data object BlockedByOfflineMode : SyncResult
    data class Failure(val reason: String) : SyncResult
}

class CurrencyRepository(
    private val dao: ExchangeRateDao,
    private val api: PublicRatesApi,
    private val moshi: Moshi,
    private val preferencesManager: PreferencesManager,
    private val networkMonitor: NetworkMonitor
) {
    val allRatesFlow: Flow<List<ExchangeRateEntity>> = dao.observeAllRates()
    val boardCurrenciesFlow: Flow<List<BoardCurrencyEntity>> = dao.observeBoardCurrencies()
    val preferencesFlow: Flow<UserPreferences> = preferencesManager.preferencesFlow
    val isOnlineFlow: Flow<Boolean> = networkMonitor.isOnlineFlow

    /**
     * Populates Room with baseline currencies and default converter board on first launch
     * so offline conversion works immediately without requiring a network connection.
     */
    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (dao.getRateCount() == 0) {
            val now = System.currentTimeMillis()
            val seedRates = CurrencyCatalog.currencies.map { info ->
                ExchangeRateEntity(
                    code = info.code,
                    rateFromUsd = info.defaultRateFromUsd,
                    customRateFromUsd = null,
                    source = "BUNDLED",
                    updatedAtMillis = now
                )
            }
            dao.upsertRates(seedRates)
        }
        if (dao.getBoardCount() == 0) {
            val seedBoard = CurrencyCatalog.defaultBoardCodes.mapIndexed { idx, code ->
                BoardCurrencyEntity(code = code, position = idx)
            }
            dao.upsertBoardCurrencies(seedBoard)
        }
    }

    /**
     * Fetches fresh public exchange rates from Open ER-API and/or Frankfurter ECB,
     * caches them in Room for offline usage, and preserves any custom calibrated overrides.
     */
    suspend fun syncPublicRates(
        provider: RateProvider,
        pureOfflineMode: Boolean
    ): SyncResult = withContext(Dispatchers.IO) {
        if (pureOfflineMode) {
            return@withContext SyncResult.BlockedByOfflineMode
        }

        val fetched: Pair<Map<String, Double>, String> = when (provider) {
            RateProvider.ER_API -> {
                fetchFromErApi() ?: return@withContext SyncResult.Failure("无法连接 Open ER-API")
            }
            RateProvider.FRANKFURTER -> {
                fetchFromFrankfurter() ?: return@withContext SyncResult.Failure("无法连接 Frankfurter 数据源")
            }
            RateProvider.AUTO -> {
                fetchFromErApi()
                    ?: fetchFromFrankfurter()
                    ?: return@withContext SyncResult.Failure("公开汇率服务暂不可达")
            }
        }

        val (ratesMap, sourceLabel) = fetched
        val now = System.currentTimeMillis()
        val existingByCode = dao.getAllRatesOnce().associateBy { it.code }

        val mergedEntities = buildList {
            // Ensure USD is always anchored at 1.0
            val normalizedMap = LinkedHashMap<String, Double>(ratesMap.size + 1)
            normalizedMap["USD"] = 1.0
            ratesMap.forEach { (code, value) ->
                if (value > 0.0 && value.isFinite()) {
                    normalizedMap[code.uppercase()] = value
                }
            }

            // Keep all catalog currencies plus any extra returned currencies
            val allCodes = linkedSetOf<String>()
            allCodes.addAll(CurrencyCatalog.currencies.map { it.code })
            allCodes.addAll(normalizedMap.keys)

            for (code in allCodes) {
                val existing = existingByCode[code]
                val newPublicRate = normalizedMap[code]
                if (newPublicRate != null) {
                    add(
                        ExchangeRateEntity(
                            code = code,
                            rateFromUsd = newPublicRate,
                            customRateFromUsd = existing?.customRateFromUsd,
                            source = sourceLabel,
                            updatedAtMillis = now
                        )
                    )
                } else if (existing != null) {
                    add(existing)
                }
            }
        }

        dao.upsertRates(mergedEntities)
        preferencesManager.recordSync(now, sourceLabel)
        SyncResult.Success(updatedCount = ratesMap.size.coerceAtLeast(1), sourceLabel = sourceLabel)
    }

    private suspend fun fetchFromErApi(): Pair<Map<String, Double>, String>? {
        return try {
            val response = api.fetchOpenErApiRates()
            val rates = response.rates
            if (response.result.equals("success", ignoreCase = true) && !rates.isNullOrEmpty()) {
                rates to "Open ER-API"
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun fetchFromFrankfurter(): Pair<Map<String, Double>, String>? {
        return try {
            val response = api.fetchFrankfurterRates()
            val rates = response.rates
            if (!rates.isNullOrEmpty()) {
                rates to "Frankfurter (ECB)"
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Calibrates an offline custom rate: "1 [baseCode] = [customRatePerBase] [targetCode]".
     */
    suspend fun setCustomOfflineRate(
        targetCode: String,
        baseCode: String,
        customRatePerBase: Double
    ) = withContext(Dispatchers.IO) {
        if (customRatePerBase <= 0.0 || !customRatePerBase.isFinite()) return@withContext
        val baseEntity = dao.getRateByCode(baseCode)
        val baseFromUsd = baseEntity?.effectiveRateFromUsd
            ?: CurrencyCatalog.getOrFallback(baseCode).defaultRateFromUsd
        val targetFromUsd = baseFromUsd * customRatePerBase
        val now = System.currentTimeMillis()

        val existing = dao.getRateByCode(targetCode)
        if (existing != null) {
            dao.setCustomRate(targetCode, targetFromUsd, now)
        } else {
            dao.upsertRate(
                ExchangeRateEntity(
                    code = targetCode,
                    rateFromUsd = targetFromUsd,
                    customRateFromUsd = targetFromUsd,
                    source = "CUSTOM_OFFLINE",
                    updatedAtMillis = now
                )
            )
        }
    }

    suspend fun resetCustomOfflineRate(code: String) = withContext(Dispatchers.IO) {
        dao.setCustomRate(code, null, System.currentTimeMillis())
    }

    /**
     * Exports all locally cached effective rates into a compact JSON string
     * so another offline device can import and update its rates without internet.
     */
    suspend fun exportOfflinePackJson(sourceLabel: String): String = withContext(Dispatchers.IO) {
        val rates = dao.getAllRatesOnce()
        val rateMap = linkedMapOf<String, Double>()
        rates.sortedBy { it.code }.forEach { entity ->
            rateMap[entity.code] = entity.effectiveRateFromUsd
        }
        val pack = OfflineRatePack(
            updatedAt = System.currentTimeMillis(),
            source = sourceLabel,
            rates = rateMap
        )
        moshi.adapter(OfflineRatePack::class.java).toJson(pack)
    }

    /**
     * Parses a JSON offline rate pack and updates the local Room database completely offline.
     */
    suspend fun importOfflinePackJson(rawJson: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val adapter = moshi.adapter(OfflineRatePack::class.java)
            val pack = adapter.fromJson(rawJson.trim())
                ?: return@withContext Result.failure(IllegalArgumentException("Empty pack"))
            val validRates = pack.rates.filter { (code, value) ->
                code.length in 3..5 && value > 0.0 && value.isFinite()
            }
            if (validRates.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("No valid rates"))
            }

            val existing = dao.getAllRatesOnce().associateBy { it.code }
            val timestamp = if (pack.updatedAt > 0L) pack.updatedAt else System.currentTimeMillis()
            val entities = validRates.map { (code, rate) ->
                val upper = code.uppercase()
                ExchangeRateEntity(
                    code = upper,
                    rateFromUsd = rate,
                    customRateFromUsd = existing[upper]?.customRateFromUsd,
                    source = "离线包导入",
                    updatedAtMillis = timestamp
                )
            }
            dao.upsertRates(entities)
            preferencesManager.recordSync(timestamp, "离线包导入")
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToBoard(code: String) = withContext(Dispatchers.IO) {
        val upper = code.uppercase()
        val current = dao.getBoardCurrenciesOnce()
        if (current.any { it.code == upper }) return@withContext
        val nextPos = (current.maxOfOrNull { it.position } ?: -1) + 1
        dao.upsertBoardCurrency(BoardCurrencyEntity(code = upper, position = nextPos))
    }

    suspend fun removeFromBoard(code: String) = withContext(Dispatchers.IO) {
        val current = dao.getBoardCurrenciesOnce()
        if (current.size <= 2) return@withContext // Always keep at least 2 currencies for conversion
        dao.removeBoardCurrency(code.uppercase())
    }

    suspend fun replaceBoardCurrency(oldCode: String, newCode: String) = withContext(Dispatchers.IO) {
        val oldUpper = oldCode.uppercase()
        val newUpper = newCode.uppercase()
        if (oldUpper == newUpper) return@withContext
        val current = dao.getBoardCurrenciesOnce().toMutableList()
        val oldIndex = current.indexOfFirst { it.code == oldUpper }
        if (oldIndex == -1) return@withContext

        val existingNewIndex = current.indexOfFirst { it.code == newUpper }
        if (existingNewIndex != -1) {
            // Swap positions cleanly if target currency is already on the board
            val posOld = current[oldIndex].position
            val posNew = current[existingNewIndex].position
            dao.upsertBoardCurrencies(
                listOf(
                    BoardCurrencyEntity(oldUpper, posNew),
                    BoardCurrencyEntity(newUpper, posOld)
                )
            )
        } else {
            val pos = current[oldIndex].position
            dao.removeBoardCurrency(oldUpper)
            dao.upsertBoardCurrency(BoardCurrencyEntity(newUpper, pos))
        }
    }

    suspend fun setActiveCurrencyCode(code: String) = preferencesManager.setActiveCurrencyCode(code)
    suspend fun setPureOfflineMode(enabled: Boolean) = preferencesManager.setPureOfflineMode(enabled)
    suspend fun setAutoUpdateOnLaunch(enabled: Boolean) = preferencesManager.setAutoUpdateOnLaunch(enabled)
    suspend fun setPreferredProvider(provider: RateProvider) = preferencesManager.setPreferredProvider(provider)
}
