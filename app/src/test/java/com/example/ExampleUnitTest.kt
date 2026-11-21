package com.example

import com.example.domain.ExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun evaluatesOperatorPrecedenceCorrectly() {
        assertEquals(2050.0, ExpressionEvaluator.evaluate("1200+850"), 0.0001)
        assertEquals(540.0, ExpressionEvaluator.evaluate("500×1.08"), 0.0001)
        assertEquals(14.0, ExpressionEvaluator.evaluate("2+3×4"), 0.0001)
        assertEquals(250.0, ExpressionEvaluator.evaluate("1000÷4"), 0.0001)
    }

    @Test
    fun handlesTrailingOperatorsAndDivisionByZeroSafely() {
        assertEquals(1200.0, ExpressionEvaluator.evaluate("1200+"), 0.0001)
        assertEquals(100.0, ExpressionEvaluator.evaluate("100÷0"), 0.0001)
    }

    @Test
    fun formatsConvertedAmountsAndRatesCleanly() {
        assertEquals("1,234.56", ExpressionEvaluator.formatConvertedAmount(1234.56, 2))
        assertEquals("15,420", ExpressionEvaluator.formatConvertedAmount(15420.4, 0))
        assertEquals("0.1381", ExpressionEvaluator.formatUnitRate(0.13812))
    }
}
