package com.github.thibseisel.mangabind

import org.junit.Assert.*
import org.junit.Test

class NaturalNumbersTest {

    @Test
    fun whenNotSpecified_genetorStartsAtZero() {
        val gen = NaturalNumbers()
        assertEquals(0, gen.nextInt())
    }

    @Test
    fun whenSpecified_generatorStartsAtGivenNumber() {
        var gen = NaturalNumbers(-42)
        assertEquals(-42, gen.nextInt())

        gen = NaturalNumbers(42)
        assertEquals(42, gen.nextInt())
    }

    @Test
    fun iteratesUntilMaximumValue() {
        val gen = NaturalNumbers(startValue = 99, maxValue = 100)
        assertTrue(gen.hasNext())

        gen.nextInt()
        assertTrue(gen.hasNext())

        gen.nextInt()
        assertFalse(gen.hasNext())
    }

    @Test
    fun valueIncrementsAtEachIteration() {
        var gen = NaturalNumbers()
        assertEquals(0, gen.nextInt())
        assertEquals(1, gen.nextInt())
        assertEquals(2, gen.nextInt())

        gen = NaturalNumbers(10)
        assertEquals(10, gen.nextInt())
        assertEquals(11, gen.nextInt())
        assertEquals(12, gen.nextInt())
    }

    @Test(expected = IllegalArgumentException::class)
    fun whenStartIsMoreThanMaxValue_failsWithException() {
        NaturalNumbers(startValue = 50, maxValue = 42)
    }

}