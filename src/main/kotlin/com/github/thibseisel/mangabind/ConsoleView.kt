package com.github.thibseisel.mangabind

/**
 * The presentation layer of the application.
 * This manages basic interactions with the end-user through the text console
 * such as displaying instructions or data, or read input from the keyboard.
 */
object ConsoleView {

    private const val TABLE_HEADER = " %3s | %20s | %50s "
    private const val MANGA_LINE = "%3d | %20s | %50s "

    private val formatNumberRange = Regex("^\\d{1,3}-\\d{1,3}$")

    private fun printReadHint(hint: String) = print("$hint > ")

    /**
     * Prompts the user for the range of chapter numbers he wants to download.
     * @return A range of chapter numbers.
     */
    fun askChapterRange(): IntRange {
        var input: String
        do {
            printReadHint("Range of chapters to download (format: X-Y)")
            input = readLine()?.trim() ?: return IntRange.EMPTY
        } while (!input.matches(formatNumberRange))

        val (start, end) = input.split('-').map(String::toInt)
        return start..end
    }

    /**
     * Print a table showing details of the specified manga sources.
     * @param sources List of sources from which manga pages can be downloaded.
     */
    fun writeMangaList(sources: List<MangaSource>) {
        println(TABLE_HEADER.format("ID", "MANGA TITLE", "SOURCE BASE URL"))
        println("--------------------------------------------------------------------------------")
        for (manga in sources) {
            println(MANGA_LINE.format(
                    manga.id,
                    manga.title.take(20),
                    manga.baseUrl.take(50)
            ))
        }
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
            input = readLine()?.trim() ?: return -1L
        } while (!input.all(Char::isDigit))
        return input.toLong()
    }

    /**
     * Writes the result of downloading a manga chapter to the console.
     */
    fun writeResult(result: LoadResult) {
        if (result.isSuccessful) {
            println("[SUCCESS] Chapter ${result.chapter} - ${result.pages.size} pages")
        } else {
            System.err.println("[ERROR] Chapter ${result.chapter} - ${result.error}")
        }
    }
}