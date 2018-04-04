package com.github.thibseisel.mangabind

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionsTest {

    @Test(expected = IllegalArgumentException::class)
    fun repeat_whenNegative_throwsError() {
        'a'.repeat(-1)
    }

    @Test
    fun repeat_whenZero_returnsEmptyString() {
        val actual = 'a'.repeat(0)
        assertTrue(actual.isEmpty())
    }

    @Test
    fun repeat_whenPositive_containsCharNTimes() {
        var actual = 'a'.repeat(1)
        assertEquals(1, actual.length)
        assertEquals("a", actual)

        actual = 'b'.repeat(10)
        assertEquals(10, actual.length)
        assertTrue(actual.all { it == 'b' })
    }
}