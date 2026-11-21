package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey val code: String,
    /** Public reference rate relative to 1 USD */
    val rateFromUsd: Double,
    /** Optional user-calibrated offline override relative to 1 USD */
    val customRateFromUsd: Double? = null,
    /** Source identifier: BUNDLED, ER_API, FRANKFURTER, OFFLINE_PACK */
    val source: String,
    val updatedAtMillis: Long
) {
    val effectiveRateFromUsd: Double
        get() = customRateFromUsd ?: rateFromUsd

    val isCustomCalibrated: Boolean
        get() = customRateFromUsd != null
}
