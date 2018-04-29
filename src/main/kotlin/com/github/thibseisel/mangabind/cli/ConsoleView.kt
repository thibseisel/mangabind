package com.github.thibseisel.mangabind.cli

import com.github.thibseisel.mangabind.PageResult
import com.github.thibseisel.mangabind.i18n.TranslationProvider
import com.github.thibseisel.mangabind.repeat
import com.github.thibseisel.mangabind.source.MangaSource
import kotlinx.coroutines.experimental.*
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
    private val out: PrintStream,
    private val translations: TranslationProvider
) {

    private companion object {
        const val TABLE_HEADER = "%3s | %20s | %50s "
        const val MANGA_LINE = "%3d | %20s | %50s "

        val formatNumberRange = Regex("^\\d{1,3}-\\d{1,3}$")
    }

    private data class ChapterProgressHandler(val chapter: Int, val progressJob: Job)

    private var progressHandler: ChapterProgressHandler? = null

    private fun printReadHint(hint: String) = out.print("$hint > ")

    /**
     * Prints a welcome message giving the application name and its purpose.
     */
    fun printWelcome() {
        out.println(translations.getText("cliWelcome"))
        out.println()
    }

    /**
     * Prompts the user for the range of chapter numbers he wants to download.
     * @return A range of chapter numbers.
     */
    fun askChapterRange(): IntRange {
        val chapterRangeHint = translations.getText("hintChapterRange")
        var input: String
        do {
            printReadHint(chapterRangeHint)
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
        println('-'.repeat(80))
        for (manga in sources) {
            out.println(
                MANGA_LINE.format(
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
        val mangaIdHint = translations.getText("hintMangaId")
        var input: String
        do {
            printReadHint(mangaIdHint)
            input = `in`.readLine()?.takeIf(String::isNotBlank) ?: return -1L
        } while (!input.all(Char::isDigit))
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
     * Informs users that the manga catalog is empty.
     */
    fun reportEmptyCatalog() {
        out.println(translations.getText("errorEmptyCatalog"))
    }

    /**
     * Informs that execution is completed and prevents the console from closing itself
     * until user have pressed a key.
     */
    fun reportTerminated() {
        out.println(translations.getText("infoTerminated"))
        `in`.read()
    }

    fun updateProgress(chapter: Int) {
        progressHandler?.let { handler ->
            if (handler.chapter == chapter) return
            handler.progressJob.cancel()
            out.println()
        }

        progressHandler = ChapterProgressHandler(chapter, launch {
            val chapterLine = translations.getText("chapterDownloading", chapter)
            var counter = 0

            // Displays a progress spinner along the chapter number under download.
            while (isActive) {
                out.print('\r')
                out.print(chapterLine)
                out.print(' ')

                out.print(
                    when (counter) {
                        1 -> '/'
                        2 -> '-'
                        3 -> '\\'
                        else -> '|'
                    }
                )

                counter = (counter + 1) % 4
                delay(125L)
            }
        })
    }

    fun writeChapterResult(chapter: Int, pages: List<PageResult>, error: Throwable?) = runBlocking {
        progressHandler?.progressJob?.let {
            it.cancelAndJoin()
            out.print('\r')
        }

        progressHandler = null

        val lastPageNumber = pages.lastOrNull()?.let { if (it.isDoublePage) it.page + 1 else it.page } ?: 0

        if (error == null) {
            val message = translations.getText("resultChapterSuccess", chapter, lastPageNumber)
            out.println(message.padEnd(79, ' '))
            val missingPages = pages.asSequence()
                .filterNot(PageResult::isSuccessful)
                .map { if (it.isDoublePage) "${it.page}-${it.page + 1}" else it.page.toString() }
                .joinToString(", ")

            if (missingPages.isNotEmpty()) {
                out.println(translations.getText("resultMissingPages", missingPages))
            }

        } else {
            val message = translations.getText("resultChapterError", chapter)
            out.println(message.padEnd(79, ' '))
            out.println(error.localizedMessage)
        }
    }
}