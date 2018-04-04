package com.github.thibseisel.mangabind

import com.github.thibseisel.mangabind.source.LocalJsonCatalogLoader
import com.github.thibseisel.mangabind.source.MangaSource
import com.github.thibseisel.mangabind.source.SourceLoader
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.actor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.InputStream

object MangaBind {

    private val console = ConsoleView
    private val httpClient = OkHttpClient()
    private val parentFolder = File("pages")

    private val sourceCatalog: SourceLoader = LocalJsonCatalogLoader()

    private val resultReporter = actor<LoadResult>(start = CoroutineStart.LAZY) {
        for (result in channel) {
            console.writeResult(result)
        }
    }

    /**
     * Execute the application.
     */
    fun run() {
        // Load manga sources from catalog.
        val sources = try {
            sourceCatalog.loadAll().sortedBy { it.id }
        } catch (ioe: IOException) {
            val message = ioe.message ?: "Error while loading manga sources catalog."
            console.showErrorMessage(message)
            return
        }

        console.displayMangaList(sources)

        var pickedSource: MangaSource? = null
        while (pickedSource == null) {
            val sourceId = console.askSourceId()
            if (sourceId < 0) return
            pickedSource = sources.firstOrNull { it.id == sourceId }
        }

        parentFolder.mkdir()
        val chapterRange = console.askChapterRange()
        for (chapter in chapterRange) {
            loadChapter(pickedSource, chapter)
        }
    }

    /**
     * Free up resources allocated by the application.
     * To be called when execution is finished.
     */
    fun cleanup() {
        resultReporter.close()
    }

    private fun loadChapter(source: MangaSource, chapter: Int): Unit = runBlocking {
        val chapterDownloadJob = Job()
        val destFilename = source.title.filterNot(Char::isWhitespace) + "_%02d_%02d.%s"
        val destFilenameDoublePage = source.title.filterNot(Char::isWhitespace) + "_%02d_%02d-%02d.%s"
        var page = source.startPage

        try {
            page@ while (true) {

                url@ for (template in source.singlePages) {
                    val url = buildUrl(template, chapter, page)
                    val imageStream = attemptConnection(url) ?: continue@url

                    launch(parent = chapterDownloadJob) {
                        val pageNumber = page
                        val filename = destFilename.format(chapter, pageNumber, url.substringAfterLast('.'))
                        val destFile = File("pages", filename)
                        writeTo(destFile, imageStream)
                        resultReporter.send(LoadResult(true, chapter, pageNumber..pageNumber))
                    }

                    page++
                    continue@page
                }

                if (source.doublePages != null) {
                    url@ for (template in source.doublePages) {
                        val url = buildUrl(template, chapter, page)
                        val imageStream = attemptConnection(url) ?: continue@url

                        launch(parent = chapterDownloadJob) {
                            val pageNumber = page
                            val filename = destFilenameDoublePage.format(
                                chapter,
                                pageNumber,
                                pageNumber + 1,
                                url.substringAfterLast('.')
                            )

                            val destFile = File("pages", filename)
                            writeTo(destFile, imageStream)
                            resultReporter.send(LoadResult(true, chapter, pageNumber..pageNumber + 1))
                        }

                        page += 2
                        continue@page
                    }
                }

                resultReporter.send(LoadResult(false, chapter, page..page))
                break@page
            }

            chapterDownloadJob.joinChildren()

        } catch (ioe: IOException) {
            val error = "Error while loading chapter $chapter of ${source.title}: ${ioe.message}"
            console.showErrorMessage(error)
            chapterDownloadJob.cancelAndJoin()
        }
    }

    @Throws(IOException::class)
    private fun attemptConnection(url: String): InputStream? {
        val response = httpClient.newCall(
            Request.Builder()
                .url(url)
                .build()
        ).execute()

        return when {
            response.isSuccessful -> response.body()?.byteStream()
            404 == response.code() -> null
            else -> throw IOException("Unexpected HTTP error code: ${response.code()}")
        }
    }

    @Throws(IOException::class)
    private fun writeTo(file: File, imageBytes: InputStream) {
        val buffer = ByteArray(8 * 1024)
        file.outputStream().use { out ->
            var read: Int = imageBytes.read(buffer)
            while (read != -1) {
                out.write(buffer, 0, read)
                read = imageBytes.read(buffer)
            }
        }
    }
}

fun main(args: Array<String>) {
    MangaBind.run()
    MangaBind.cleanup()
}

class LoadResult(
    val isSuccessful: Boolean,
    val chapter: Int,
    val pages: IntRange
)