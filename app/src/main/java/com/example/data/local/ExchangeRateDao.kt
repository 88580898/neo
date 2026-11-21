package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rates ORDER BY code ASC")
    fun observeAllRates(): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates")
    suspend fun getAllRatesOnce(): List<ExchangeRateEntity>

    @Query("SELECT * FROM exchange_rates WHERE code = :code LIMIT 1")
    suspend fun getRateByCode(code: String): ExchangeRateEntity?

    @Query("SELECT COUNT(*) FROM exchange_rates")
    suspend fun getRateCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRates(rates: List<ExchangeRateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRate(rate: ExchangeRateEntity)

    @Query("UPDATE exchange_rates SET customRateFromUsd = :customRate, updatedAtMillis = :updatedAt WHERE code = :code")
    suspend fun setCustomRate(code: String, customRate: Double?, updatedAt: Long)

    @Query("SELECT * FROM board_currencies ORDER BY position ASC")
    fun observeBoardCurrencies(): Flow<List<BoardCurrencyEntity>>

    @Query("SELECT * FROM board_currencies ORDER BY position ASC")
    suspend fun getBoardCurrenciesOnce(): List<BoardCurrencyEntity>

    @Query("SELECT COUNT(*) FROM board_currencies")
    suspend fun getBoardCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBoardCurrencies(items: List<BoardCurrencyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBoardCurrency(item: BoardCurrencyEntity)

    @Query("DELETE FROM board_currencies WHERE code = :code")
    suspend fun removeBoardCurrency(code: String)
}
