package com.github.thibseisel.mangabind

import java.io.File
import java.net.MalformedURLException
import java.net.URL

/**
 * Creates a new String composed of this character repeated [n] times.
 * This is more efficient than [String.repeat] for strings of only one character.
 *
 * @receiver The character to be repeated.
 * @param n The number of times the character should be repeated, must be non-negative.
 * @return A String composed of the receiver character repeated [n] times.
 */
fun Char.repeat(n: Int): String {
    require(n >= 0) { "Count 'n' must be non-negative, but was $n" }
    return when (n) {
        0 -> ""
        1 -> toString()
        else -> String(CharArray(n) {this})
    }
}

/**
 * Parse a String as an [URL].
 *
 * @receiver The string representation of a valid URL.
 * @return An Uniform Resource Locator object.
 * @throws MalformedURLException If no protocol is specified, or an unknown protocol is found.
 */
@Throws(MalformedURLException::class)
fun String.toUrl(): URL = URL(this)

val File.isImageFile get() = isFile && extension.let {
    it.endsWith("jpg") || it.endsWith("png") || it.endsWith("jpeg")
}