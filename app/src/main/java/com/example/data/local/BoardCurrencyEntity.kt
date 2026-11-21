package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "board_currencies")
data class BoardCurrencyEntity(
    @PrimaryKey val code: String,
    val position: Int
)
