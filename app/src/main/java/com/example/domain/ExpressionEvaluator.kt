package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Lightweight, fault-tolerant arithmetic expression evaluator and financial formatter
 * for instant currency conversion keypad input.
 */
object ExpressionEvaluator {

    private val operators = setOf('+', '-', '×', '÷')

    fun hasOperator(expression: String): Boolean {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return false
        return trimmed.drop(1).any { it in operators }
    }

    /**
     * Evaluates expressions like "1200+850", "500×1.08", "1000÷4" with standard operator precedence.
     * Trailing operators are safely ignored so intermediate typing produces live conversion results.
     */
    fun evaluate(expression: String): Double {
        val clean = expression.replace(",", "").trim()
        if (clean.isEmpty()) return 0.0

        val tokens = tokenize(clean)
        if (tokens.isEmpty()) return 0.0

        // Pass 1: resolve × and ÷
        val reducedNumbers = mutableListOf<Double>()
        val reducedOps = mutableListOf<Char>()

        var current = tokens.firstOrNull()?.toDoubleOrNull() ?: return 0.0
        var i = 1
        while (i < tokens.size) {
            val op = tokens[i].firstOrNull() ?: break
            val nextNum = tokens.getOrNull(i + 1)?.toDoubleOrNull()
            if (nextNum == null) {
                break // Trailing operator — evaluate everything before it
            }
            when (op) {
                '×' -> current *= nextNum
                '÷' -> current = if (nextNum == 0.0) current else current / nextNum
                '+', '-' -> {
                    reducedNumbers.add(current)
                    reducedOps.add(op)
                    current = nextNum
                }
            }
            i += 2
        }
        reducedNumbers.add(current)

        // Pass 2: resolve + and -
        var result = reducedNumbers.first()
        for (idx in reducedOps.indices) {
            val rhs = reducedNumbers[idx + 1]
            result = when (reducedOps[idx]) {
                '+' -> result + rhs
                '-' -> result - rhs
                else -> result
            }
        }
        return if (result.isNaN() || result.isInfinite()) 0.0 else result.coerceAtLeast(0.0)
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val numberBuffer = StringBuilder()

        for (ch in input) {
            when {
                ch.isDigit() || ch == '.' -> numberBuffer.append(ch)
                ch in operators -> {
                    if (numberBuffer.isNotEmpty()) {
                        tokens.add(numberBuffer.toString())
                        numberBuffer.clear()
                    }
                    if (tokens.isNotEmpty()) {
                        if (tokens.last().length == 1 && tokens.last()[0] in operators) {
                            tokens[tokens.lastIndex] = ch.toString()
                        } else {
                            tokens.add(ch.toString())
                        }
                    }
                }
            }
        }
        if (numberBuffer.isNotEmpty()) {
            tokens.add(numberBuffer.toString())
        }
        return tokens
    }

    fun appendDigit(current: String, digit: String): String {
        if (current.length >= 24) return current
        val lastNumber = current.split('+', '-', '×', '÷').lastOrNull().orEmpty()
        if (lastNumber.contains('.') && lastNumber.substringAfter('.').length >= 4) {
            return current
        }
        if (current == "0" && digit != ".") {
            return if (digit == "00") "0" else digit
        }
        if (lastNumber == "0" && digit == "00") {
            return current
        }
        return current + digit
    }

    fun appendDecimal(current: String): String {
        if (current.isEmpty()) return "0."
        val lastChar = current.last()
        if (lastChar in operators) return "${current}0."
        val lastNumber = current.split('+', '-', '×', '÷').lastOrNull().orEmpty()
        if (lastNumber.contains('.')) return current
        return "$current."
    }

    fun appendOperator(current: String, op: Char): String {
        if (current.isEmpty()) return "0$op"
        val lastChar = current.last()
        return if (lastChar in operators) {
            current.dropLast(1) + op
        } else if (lastChar == '.') {
            current.dropLast(1) + op
        } else {
            current + op
        }
    }

    fun backspace(current: String): String {
        if (current.length <= 1) return "0"
        return current.dropLast(1)
    }

    fun formatEditableResult(value: Double): String {
        if (value == 0.0) return "0"
        val bd = BigDecimal.valueOf(value)
            .setScale(4, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        return bd.toPlainString()
    }

    fun formatConvertedAmount(value: Double, fractionDigits: Int = 2): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val symbols = DecimalFormatSymbols(Locale.US)
        val absVal = abs(value)
        val pattern = when {
            absVal == 0.0 -> "0"
            absVal < 0.01 && absVal > 0.0 -> "#,##0.####"
            fractionDigits == 0 && absVal >= 100.0 -> "#,##0"
            fractionDigits == 0 -> "#,##0.##"
            else -> "#,##0.00"
        }
        return DecimalFormat(pattern, symbols).format(value)
    }

    fun formatUnitRate(rate: Double): String {
        if (rate.isNaN() || rate.isInfinite() || rate <= 0.0) return "--"
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = when {
            rate >= 1000.0 -> "#,##0.00"
            rate >= 10.0 -> "#,##0.000"
            rate >= 0.1 -> "0.0000"
            else -> "0.000000"
        }
        return DecimalFormat(pattern, symbols).format(rate)
    }

    fun formatDisplayExpression(expression: String): String {
        if (expression.isEmpty()) return "0"
        val sb = StringBuilder()
        val numBuf = StringBuilder()

        fun flushNumber() {
            if (numBuf.isEmpty()) return
            val raw = numBuf.toString()
            val parts = raw.split('.')
            val intPart = parts[0]
            val grouped = intPart.reversed().chunked(3).joinToString(",").reversed()
            sb.append(grouped)
            if (parts.size > 1) {
                sb.append('.').append(parts[1])
            }
            numBuf.clear()
        }

        for (ch in expression) {
            if (ch in operators) {
                flushNumber()
                sb.append(' ').append(ch).append(' ')
            } else {
                numBuf.append(ch)
            }
        }
        flushNumber()
        return sb.toString()
    }
}
