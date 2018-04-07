package com.github.thibseisel.mangabind

import com.github.thibseisel.mangabind.source.MangaSource
import java.io.BufferedReader
import java.io.PrintStream
import javax.inject.Inject

/**
 * The presentation layer of the application.
 * This manages basic interactions with the end-user through the text console
 * such as displaying instructions or data, or read input from the keyboard.
 */
class ConsoleView
@Inject constructor(
    private val `in`: BufferedReader,
    private val out: PrintStream
) {

    private companion object {
        const val TABLE_HEADER = "%3s | %20s | %50s "
        const val MANGA_LINE = "%3d | %20s | %50s "

        const val RESULT_LINE = "[%c] Chapter %d - Page %s"

        val formatNumberRange = Regex("^\\d{1,3}-\\d{1,3}$")
    }

    private fun printReadHint(hint: String) = out.print("$hint > ")

    /**
     * Prompts the user for the range of chapter numbers he wants to download.
     * @return A range of chapter numbers.
     */
    fun askChapterRange(): IntRange {
        var input: String
        do {
            printReadHint("Range of chapters to download (format: X-Y)")
            input = `in`.readLine()?.trim()?.takeUnless(String::isEmpty) ?: return IntRange.EMPTY
        } while (!input.matches(formatNumberRange))

        val (start, end) = input.split('-').map(String::toInt)
        return start..end
    }

    /**
     * Prints a table showing details of the specified manga sources.
     * @param sources List of sources from which manga pages can be downloaded.
     */
    fun displayMangaList(sources: List<MangaSource>) {
        println(TABLE_HEADER.format("ID", "MANGA TITLE", "SOURCE URL"))
        println("-".repeat(80))
        for (manga in sources) {
            out.println(MANGA_LINE.format(
                    manga.id,
                    manga.title.take(20),
                    manga.origin.take(50)
            ))
        }

        out.println()
    }

    /**
     * Prompts the user for the identifier of the manga source he wishes to download scans from.
     * The returned identifier is not guaranteed to match the identifier of an existing manga source.
     *
     * @return A positive or zero integer that may match the id of a manga source, or `-1` if nothing has been typed.
     */
    fun askSourceId(): Long {
        var input: String
        do {
            printReadHint("Type in the number identifier of the manga")
            input = `in`.readLine()?.trim() ?: return -1L
        } while (!input.all(Char::isDigit))
        readLine()
        return input.toLong()
    }

    /**
     * Prints a message as an error.
     * @param message The message to display.
     */
    fun showErrorMessage(message: String) {
        out.println(message)
    }

    /**
     * Writes the result of downloading a manga page to the console.
     */
    fun writeResult(result: LoadResult) {
        val pages = result.pages.joinToString("-")
        if (result.isSuccessful) {
            out.println(RESULT_LINE.format('O', result.chapter, pages))
        } else {
            out.println(RESULT_LINE.format('X', result.chapter, pages))
        }
    }
}