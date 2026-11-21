package com.example.ui

import com.example.data.CurrencyInfo
import com.example.data.local.RateProvider

enum class AppTab {
    CONVERTER,
    OFFLINE_RATES
}

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Success(val count: Int, val source: String) : SyncStatus
    data class Error(val message: String) : SyncStatus
}

data class BoardCurrencyItem(
    val info: CurrencyInfo,
    val isActive: Boolean,
    val formattedAmount: String,
    val rawAmount: Double,
    val unitRateText: String,
    val isCustomCalibrated: Boolean
)

data class OfflineRateRow(
    val info: CurrencyInfo,
    val effectiveRatePerBase: Double,
    val publicRatePerBase: Double,
    val formattedEffectiveRate: String,
    val formattedPublicRate: String,
    val isCustomCalibrated: Boolean,
    val isPinnedToBoard: Boolean,
    val source: String
)

data class CurrencyUiState(
    val boardItems: List<BoardCurrencyItem> = emptyList(),
    val allRates: List<OfflineRateRow> = emptyList(),
    val activeCurrencyCode: String = "CNY",
    val inputExpression: String = "100",
    val formattedExpressionPreview: String = "",
    val hasOperatorInExpression: Boolean = false,
    val isKeypadVisible: Boolean = true,
    val isOnline: Boolean = true,
    val pureOfflineMode: Boolean = false,
    val autoUpdateOnLaunch: Boolean = true,
    val preferredProvider: RateProvider = RateProvider.AUTO,
    val lastSyncMillis: Long = 0L,
    val lastSyncSource: String = "内置基准汇率",
    val cachedCurrencyCount: Int = 0,
    val customCalibratedCount: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.Idle,
    val offlineBaseCurrencyCode: String = "CNY",
    val offlineSearchQuery: String = "",
    val isLoadingInitial: Boolean = true
)
