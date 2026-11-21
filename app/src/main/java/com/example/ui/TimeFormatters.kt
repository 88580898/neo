package com.example.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatters {
    fun formatRelativeSyncTime(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "尚未联网更新"
        val diffMillis = (System.currentTimeMillis() - timestampMillis).coerceAtLeast(0L)
        val minutes = diffMillis / 60_000L
        val hours = minutes / 60L
        return when {
            minutes < 1L -> "刚刚更新"
            minutes < 60L -> "${minutes}分钟前更新"
            hours < 24L -> "${hours}小时前更新"
            else -> {
                val fmt = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA)
                "更新于 ${fmt.format(Date(timestampMillis))}"
            }
        }
    }

    fun formatFullTimestamp(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "内置基准（尚未联网）"
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        return fmt.format(Date(timestampMillis))
    }
}
