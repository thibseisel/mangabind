package com.github.thibseisel.mangabind

class ConsoleView {

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
     * Prompts the user for the identifier of the manga he wish to download scans from.
     * The returned identifier is not guaranteed to match the identifier of an existing manga source.
     *
     * @return A positive or zero integer that may match the id of a manga source, or `-1` if nothing has been typed.
     */
    fun askMangaId(): Long {
        var input: String
        do {
            printReadHint("Type in the number identifier of the manga")
            input = readLine()?.trim() ?: return -1L
        } while (!input.all(Char::isDigit))
        return input.toLong()
    }

    private companion object {
        const val TABLE_HEADER = " %3s | %20s | %50s "
        const val MANGA_LINE = "%3d | %20s | %50s "
    }
}