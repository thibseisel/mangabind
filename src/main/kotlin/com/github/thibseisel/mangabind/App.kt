package com.github.thibseisel.mangabind

import com.github.thibseisel.mangabind.source.LocalJsonCatalogLoader
import com.github.thibseisel.mangabind.source.SourceLoader
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import java.io.IOException

object MangaBind {

    private val console = ConsoleView
    private val httpClient = OkHttpClient()

    private val sourceCatalog: SourceLoader = LocalJsonCatalogLoader()

    private val resultReporter = actor<LoadResult>(start = CoroutineStart.LAZY) {
        for (result in channel) {
            console.writeResult(result)
        }
    }

    /**
     * Execute the application.
     */
    fun run(): Unit = runBlocking {
        // Load manga sources from catalog.
        val sources = try {
            sourceCatalog.loadAll()
        } catch (ioe: IOException) {
            val message = ioe.message ?: "Error while loading manga sources catalog."
            console.showErrorMessage(message)
            return@runBlocking
        }

        console.writeMangaList(sources)

        var pickedSource: MangaSource? = null
        while (pickedSource == null) {
            val sourceId = console.askSourceId()
            if (sourceId < 0) return@runBlocking
            pickedSource = sources.firstOrNull { it.id == sourceId }
        }

        val chapterRange = console.askChapterRange()
        /*for (chapter in chapterRange) {
            loadChapter(pickedSource, chapter)
        }*/
    }

    /**
     * Free up resources allocated by the application.
     * To be called when execution is finished.
     */
    fun cleanup() {
        resultReporter.close()
    }

    private fun loadChapter(source: MangaSource, chapterNumber: Int) {
        val downloader = ChapterDownloader(httpClient, source, chapterNumber, resultReporter)
        downloader.downloadTo("./pages")
    }
}

fun main(args: Array<String>) {
    MangaBind.run()
    MangaBind.cleanup()
}

class LoadResult(
    val chapter: Int,
    val page: Int,
    val isSuccessful: Boolean,
    val error: String? = null
)