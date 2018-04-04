package com.github.thibseisel.mangabind

private val reUrlParams = Regex("""\[(\d?[cpq])]""")

/**
 * Builds a valid URL from a template, replacing parameters specified between square brackets by their actual value.
 */
fun buildUrl(templateUrl: String, chapter: Int, page: Int): String {
    return templateUrl.replace(reUrlParams) { match ->
        val param = match.groupValues[1]

        val desiredLength = if (param[0].isDigit()) param[0] - '0' else 1
        when (param.last()) {
            'c' -> chapter
            'p' -> page
            'q' -> page + 1
            else -> throw IllegalArgumentException("Unexpected URL param: $param")
        }.toString().padStart(desiredLength, '0')
    }
}