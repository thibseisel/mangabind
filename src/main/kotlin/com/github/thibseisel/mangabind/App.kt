package com.github.thibseisel.mangabind

import com.github.thibseisel.mangabind.dagger.DaggerConsoleComponent
import com.github.thibseisel.mangabind.dagger.FilenameProviderModule
import com.github.thibseisel.mangabind.source.MangaSource
import com.github.thibseisel.mangabind.source.MangaRepository
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class Mangabind
@Inject constructor(
        private val console: ConsoleView,
        private val httpClient: OkHttpClient,
        private val sourceCatalog: MangaRepository,
        @Named("outputDir") outputDirName: String
) {

    private val outputDir = File(outputDirName)
    private val logger: Logger = LogManager.getFormatterLogger("App")

    /**
     * Execute the application.
     */
    fun run() {

        logger.info("Starting application.")

        // Load manga sources from catalog.
        val sources = try {
            sourceCatalog.getAll().sortedBy { it.id }
        } catch (ioe: IOException) {
            logger.fatal("Error while loading catalog.", ioe)
            val message = ioe.message ?: "Error while loading manga sources catalog."
            console.showErrorMessage(message)
            return
        }

        if (sources.isNotEmpty()) {
            console.displayMangaList(sources)

            var pickedSource: MangaSource? = null
            while (pickedSource == null) {
                val sourceId = console.askSourceId()
                if (sourceId < 0) return
                pickedSource = sources.firstOrNull { it.id == sourceId }
            }

            outputDir.mkdir()
            val chapterRange = console.askChapterRange()
            for (chapter in chapterRange) {
                logger.info("Start loading chapter N°%d...", chapter)
                loadChapter(pickedSource, chapter)
            }

        } else {
            logger.warn("Manga catalog is empty. Maybe should be filled ?")
            console.reportEmptyCatalog()
        }

        console.reportTerminated()
        logger.info("Application terminated normally.")
    }

    private fun loadChapter(source: MangaSource, chapter: Int): Unit = runBlocking {
        val chapterDownloadJob = Job()
        val destFilename = source.title.filterNot(Char::isWhitespace) + "_%02d_%02d.%s"
        val destFilenameDoublePage = source.title.filterNot(Char::isWhitespace) + "_%02d_%02d-%02d.%s"
        var chapterError: Throwable? = null

        val pageResultChannel = Channel<PageResult>(capacity = Channel.UNLIMITED)

        // Provide an immutable increasing page number to solve concurrency problems
        val pageIterator = NaturalNumbers(startValue = source.startPage, maxValue = 100)
        var giveLastChance = true

        // Copy available urls to LRU-lists
        val singlePages = LinkedList<String>(source.singlePages)
        val doublePages = LinkedList<String>(source.doublePages ?: emptyList())

        console.updateProgress(chapter)

        try {
            page@ while (pageIterator.hasNext()) {
                val page = pageIterator.nextInt()

                logger.debug("[%d,%02d] Matching single-page urls...", chapter, page)

                url@ for ((index, template) in singlePages.withIndex()) {
                    val url = buildUrl(template, chapter, page)
                    logger.trace(url)
                    val imageStream = attemptConnection(url) ?: continue@url

                    logger.info("[%d,%02d] Found matching URL %s", chapter, page, url)

                    // Promote the url that hit, as it is more likely to hit again.
                    singlePages.removeAt(index)
                    singlePages.addFirst(template)

                    launch(parent = chapterDownloadJob) {
                        val filename = destFilename.format(chapter, page, url.substringAfterLast('.'))
                        imageStream.buffered().use {
                            val destFile = File("pages", filename)
                            writeTo(destFile, it)
                        }
                        pageResultChannel.send(PageResult(true, chapter, page))
                    }

                    // Restore last chance
                    giveLastChance = true

                    // Skipping to the next page is done at each iteration.
                    continue@page
                }

                logger.debug("[%d,%02d] Matching double-page urls...", chapter, page)

                url@ for ((index, template) in doublePages.withIndex()) {
                    val url = buildUrl(template, chapter, page)
                    logger.trace(url)
                    val imageStream = attemptConnection(url) ?: continue@url

                    logger.info("[%d,%02d] Found matching URL %s", chapter, page, url)

                    // Promote the url that hit, as it is more likely to hit again.
                    doublePages.removeAt(index)
                    doublePages.addFirst(template)

                    launch(parent = chapterDownloadJob) {
                        val filename = destFilenameDoublePage.format(
                            chapter,
                            page,
                            page + 1,
                            url.substringAfterLast('.')
                        )

                        imageStream.buffered().use {
                            val destFile = File("pages", filename)
                            writeTo(destFile, it)
                        }

                        pageResultChannel.send(PageResult(true, chapter, page, isDoublePage = true))
                    }

                    // Restore last chance
                    giveLastChance = true
                    
                    // Manually skip one more page to increment the page counter by 2.
                    pageIterator.nextInt()
                    continue@page
                }

                if (giveLastChance) {
                    logger.info("[%d,%02d] No matching URL. Check for a \"missing page\" scenario...", chapter, page)
                    pageResultChannel.send(PageResult(false, chapter, page))
                    giveLastChance = false
                    continue@page
                }

                logger.info("[%d,%02d] No matching URL found.", chapter, page)
                break@page
            }

            logger.debug("Waiting for download tasks to complete...")
            chapterDownloadJob.joinChildren()

            logger.info("Finished downloading chapter %s.", chapter)

        } catch (ioe: IOException) {
            logger.error("Unexpected error while loading chapter %d.", chapter, ioe)
            chapterDownloadJob.cancelAndJoin()
            chapterError = ioe
        }

        pageResultChannel.close()
        val chapterPagesResult = pageResultChannel.toList()
            .sortedBy(PageResult::page)
            .dropLastWhile { !it.isSuccessful }
        console.writeChapterResult(chapter, chapterPagesResult, chapterError)
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
        file.outputStream().buffered().use { out ->
            var read: Int = imageBytes.read(buffer)
            while (read != -1) {
                out.write(buffer, 0, read)
                read = imageBytes.read(buffer)
            }
        }
    }
}

fun main(args: Array<String>) {
    val appComponent = DaggerConsoleComponent.builder()
        .filenameProviderModule(FilenameProviderModule("pages"))
        .build()
    appComponent.mangabind.run()
}

class PageResult(
    val isSuccessful: Boolean,
    val chapter: Int,
    val page: Int,
    val isDoublePage: Boolean = false
)