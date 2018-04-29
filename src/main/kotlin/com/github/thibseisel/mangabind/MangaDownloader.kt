package com.github.thibseisel.mangabind

import com.github.thibseisel.mangabind.source.MangaSource
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.toList
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class MangaDownloader
@Inject constructor(
    private val httpClient: OkHttpClient,
    @Named("outputDir") outputDirName: String
) {

    private companion object {

        /**
         * The template filename for each downloaded page.
         * It must be formatted using [String.format] with the following parameters:
         * 1. The number of the chapter this page belongs to
         * 2. The number of that page
         * 3. The file extension of the downloaded image.
         */
        private const val PAGE_FILENAME = "%02d_%02d.%s"

        /**
         * The template filename specific to downloaded double-pages.
         * It must be formatted using [String.format] with the following parameters:
         * 1. The number of the chapter this page belongs to
         * 2. The number of the first of that double page
         * 3. The number of the facing page
         * 4. The file extension of the downloaded image.
         */
        private const val DOUBLE_PAGE_FILENAME = "%02d_%02d-%02d.%s"
    }

    private val logger = LogManager.getFormatterLogger("MangaDownloader")

    /**
     * The parent directory where all downloaded pages should be stored.
     */
    private val outputDir = File(outputDirName).also {
        check(!it.exists() || it.isDirectory) { "Output exists but is not a directory" }
    }

    fun loadChapterAsync(manga: MangaSource, chapter: Int) = async<ChapterResult> {
        val chapterDownloadJob = Job()
        val pageResultChannel = Channel<PageResult>(capacity = Channel.UNLIMITED)
        var chapterError: Throwable? = null

        // Provide an immutable increasing page number to solve concurrency problems.
        val pageIterator = NaturalNumbers(startValue = manga.startPage)
        var giveLastChance = true

        // Copy available urls to LRU-lists
        val singlePages = LinkedList<String>(manga.singlePages)
        val doublePages = LinkedList<String>(manga.doublePages.orEmpty())

        outputDir.mkdir()

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
                        val filename = PAGE_FILENAME.format(chapter, page, url.substringAfterLast('.'))
                        imageStream.use {
                            val destFile = File(outputDir, filename)
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
                        val filename = DOUBLE_PAGE_FILENAME.format(
                            chapter,
                            page,
                            page + 1,
                            url.substringAfterLast('.')
                        )

                        imageStream.buffered().use {
                            val destFile = File(outputDir, filename)
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

        ChapterResult(chapter, chapterPagesResult, chapterError)
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