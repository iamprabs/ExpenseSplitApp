package com.prabs.ceipts.domain.usecase

import com.prabs.ceipts.domain.model.ShareType
import org.junit.Assert.assertEquals
import org.junit.Test

class SplitCalculatorTest {

    private val calculator = SplitCalculator()

    @Test
    fun testEqualSplit_EvenDivision() {
        val total = 100.0
        val specs = listOf(
            SplitSpec("user1", ShareType.EQUAL),
            SplitSpec("user2", ShareType.EQUAL)
        )
        val result = calculator.calculateSplits(total, "user1", specs)
        
        assertEquals(50.0, result[0].computedAmount, 0.0)
        assertEquals(50.0, result[1].computedAmount, 0.0)
        assertEquals(total, result.sumOf { it.computedAmount }, 0.0)
    }

    @Test
    fun testEqualSplit_OddDivision_WithRemainder() {
        val total = 100.0
        val specs = listOf(
            SplitSpec("user1", ShareType.EQUAL),
            SplitSpec("user2", ShareType.EQUAL),
            SplitSpec("user3", ShareType.EQUAL)
        )
        // 100 / 3 = 33.33 each. Total = 99.99. Difference = 0.01
        // Payer (user1) should get the extra 0.01, so 33.34
        val result = calculator.calculateSplits(total, "user1", specs)
        
        assertEquals(33.34, result[0].computedAmount, 0.0) // payer
        assertEquals(33.33, result[1].computedAmount, 0.0)
        assertEquals(33.33, result[2].computedAmount, 0.0)
        assertEquals(total, result.sumOf { it.computedAmount }, 0.0)
    }

    @Test
    fun testExactSplit() {
        val total = 100.0
        val specs = listOf(
            SplitSpec("user1", ShareType.EXACT, 20.0),
            SplitSpec("user2", ShareType.EXACT, 80.0)
        )
        val result = calculator.calculateSplits(total, "user1", specs)
        
        assertEquals(20.0, result[0].computedAmount, 0.0)
        assertEquals(80.0, result[1].computedAmount, 0.0)
    }

    @Test
    fun testPercentSplit() {
        val total = 200.0
        val specs = listOf(
            SplitSpec("user1", ShareType.PERCENT, 10.0),
            SplitSpec("user2", ShareType.PERCENT, 90.0)
        )
        val result = calculator.calculateSplits(total, "user1", specs)
        
        assertEquals(20.0, result[0].computedAmount, 0.0)
        assertEquals(180.0, result[1].computedAmount, 0.0)
        assertEquals(total, result.sumOf { it.computedAmount }, 0.0)
    }

    @Test
    fun testSharesSplit() {
        val total = 150.0
        val specs = listOf(
            SplitSpec("user1", ShareType.SHARES, 1.0),
            SplitSpec("user2", ShareType.SHARES, 2.0)
        )
        // Total shares = 3. user1 = 1/3, user2 = 2/3.
        // 150 * 1/3 = 50. 150 * 2/3 = 100.
        val result = calculator.calculateSplits(total, "user1", specs)
        
        assertEquals(50.0, result[0].computedAmount, 0.0)
        assertEquals(100.0, result[1].computedAmount, 0.0)
        assertEquals(total, result.sumOf { it.computedAmount }, 0.0)
    }
}
