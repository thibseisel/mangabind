package com.github.thibseisel.mangabind

import kotlinx.coroutines.experimental.channels.SendChannel
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.annotations.Contract
import java.io.File
import java.io.IOException
import java.io.InputStream

private val reUrlParams = Regex("""\[(\d?[cpq])]""")

@Contract(pure = true)
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

class ChapterDownloader(
    private val httpClient: OkHttpClient,
    private val source: MangaSource,
    private val chapter: Int,
    private val resultReceiver: SendChannel<LoadResult>
) {

    private val pageSequence: Sequence<Int> = generateSequence(source.startPage) { it + 1 }
    private var shouldSkip = false

    /**
     * The destination filename for a single page.
     * Composed of the title of the manga without spaces, its chapter name, its page number
     * and the file extension.
     *
     * Parameters for [String.format]:
     * 1. (Int) chapter number
     * 2. (Int) page number
     * 3. (String) file extension, based on the successful network url.
     */
    private val destFilenameSinglePage = source.title.filterNot(Char::isWhitespace) + "_%02d_%02d.%s"

    /**
     * The destination filename for a double-page.
     * Composed of the title of the manga without spaces, its chapter name, its page number joined by a hyphens
     * and the file extension.
     *
     * Parameters for [String.format]:
     * 1. (Int) chapter number
     * 2. (Int) first page number
     * 3. (Int) second page number
     * 4. (String) file extension, base of the successful network url.
     */
    private val destFilenameDoublePage = source.title.filterNot(Char::isWhitespace) + "_%02d_%02d-%02d.%s"

    fun downloadTo(folderPath: String) {

        page@ for (page in pageSequence) {

            // Skip a page if the last one was a double page
            if (shouldSkip) {
                shouldSkip = false
                continue
            }

            for (templateUrl in source.singlePages) {
                val url = buildUrl(templateUrl, chapter, page)
                val imageStream = loadImage(url)
                if (imageStream != null) {
                    val destFilename = buildFilename(url, chapter, page)
                    writeToFile(File(folderPath, destFilename), imageStream)
                    continue@page
                }
            }

            // Attempt to load using double page urls
            if (source.doublePages != null) {
                for (templateUrl in source.doublePages) {
                    val url = buildUrl(templateUrl, chapter, page)
                    val imageStream = loadImage(url)
                    if (imageStream != null) {
                        val destFilename = buildFilename(url, chapter, page, page + 1)
                        writeToFile(File(folderPath, destFilename), imageStream)

                        shouldSkip = true
                        continue@page
                    }
                }
            }

            // All strategies have failed. This is probably the end of the chapter.
            break
        }
    }

    @Contract(pure = true)
    private fun buildFilename(networkUrl: String, chapter: Int, vararg pages: Int): String {
        val fileExtension = networkUrl.substringAfterLast('.')
        return if (pages.size == 1) {
            val page = pages[0]
            destFilenameSinglePage.format(chapter, page, fileExtension)
        } else {
            val (first, second) = pages
            destFilenameDoublePage.format(chapter, first, second, fileExtension)
        }
    }

    @Throws(IOException::class)
    private fun writeToFile(destFile: File, input: InputStream) {
        val buffer = ByteArray(8 * 1024)
        destFile.outputStream().use { file ->
            var read: Int
            do {
                read = input.read(buffer)
                file.write(buffer, 0, read)
            } while (read != -1)
        }
    }

    @Throws(IOException::class)
    private fun loadImage(url: String): InputStream? {
        try {
            val response = httpClient.newCall(
                Request.Builder()
                    .url(url)
                    .build()
            ).execute()
            if (!response.isSuccessful) return null
            return response.body()?.byteStream()

        } catch (ioe: IOException) {
            return null
        }
    }
}