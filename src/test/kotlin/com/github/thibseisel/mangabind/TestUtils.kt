package com.github.thibseisel.mangabind

import org.junit.Assert.fail

inline fun <reified T: Exception> assertThrows(block: () -> Unit) {
    try {
        block()
        fail("Expecting an exception of type ${T::class.java.simpleName} but none was thrown.")
    } catch (e: Exception) {
        if (e !is T) {
            fail("Expecting an exception of type ${T::class.java.simpleName} but was ${e::class.java.simpleName}")
        }
    }
}