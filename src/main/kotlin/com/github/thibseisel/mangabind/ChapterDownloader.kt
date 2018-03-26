package com.github.thibseisel.mangabind

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.annotations.Contract
import java.io.File
import java.io.IOException
import java.io.InputStream

private val reUrlParams = Regex("""\[(\d?[cp][12]?)]""")

fun buildUrl(template: String, chapter: Int, page: Int): String {
    return template.replace(reUrlParams) { match ->
        val param = match.groupValues[1]
        val indexOfC = param.indexOf('c')
        val indexOfP = param.indexOf('p')

        when {
            indexOfC != -1 -> {
                val zeroPadding = if (indexOfC == 0) 1 else param[0] - '0'
                chapter.toString().padStart(zeroPadding, '0')
            }
            indexOfP != -1 -> {
                val zeroPadding = if (indexOfP == 0) 1 else param[0] - '0'
                val pageIncrement = param.getOrElse(indexOfP + 1) {'1'} - '1'
                (page + pageIncrement).toString().padStart(zeroPadding, '0')
            }

            else -> throw AssertionError()
        }
    }
}

class ChapterDownloader(
    private val httpClient: OkHttpClient,
    private val source: MangaSource,
    private val chapter: Int
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
        val singlePageUrls = source.singlePages?.asSequence()?.map { source.baseUrl + it }
        val doublePageUrls = source.doublePages?.asSequence()?.map { source.baseUrl + it }

        page@ for (page in pageSequence) {

            // Skip a page if the last one was a double page
            if (shouldSkip) {
                shouldSkip = false
                continue
            }

            // Attempt to load using single page urls
            if (singlePageUrls != null) {
                singlePageUrls.map {
                    val url = buildUrl(it, chapter, page)
                    try {
                        val imageBytes = loadImage(url)
                        val destFilename = buildFilename(url, chapter, page)
                        writeToFile(File(folderPath, destFilename), imageBytes)
                        true
                    } catch (e: IOException) {
                        false
                    }
                }.first { it }
            }

            // Attempt to load using double page urls
            if (doublePageUrls != null) {

                shouldSkip = true
                continue
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
            var read = 0
            do {
                read = input.read(buffer)
                file.write(buffer, 0, read)
            } while (read != -1)
        }
    }

    @Throws(IOException::class)
    private fun loadImage(url: String): InputStream {
        val response = httpClient.newCall(
            Request.Builder()
                .url(url)
                .build()
        ).execute()

        if (!response.isSuccessful) throw IOException("Unexpected code: ${response.code()}")
        return response.body()!!.byteStream()
    }
}