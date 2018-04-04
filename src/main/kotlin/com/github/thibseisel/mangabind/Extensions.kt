package com.github.thibseisel.mangabind

fun Char.repeat(times: Int): String {
    require(times >= 0) { "Count 'times' must be non-negative, but was $times" }
    return when (times) {
        0 -> ""
        1 -> toString()
        else -> buildString(times) {
            repeat(times) {
                append(this@repeat)
            }
        }
    }
}