package com.github.thibseisel.mangabind.cli

import com.github.thibseisel.mangabind.MangaDownloader
import com.github.thibseisel.mangabind.dagger.FilenameProviderModule
import com.github.thibseisel.mangabind.source.MangaRepository
import com.github.thibseisel.mangabind.source.MangaSource
import kotlinx.coroutines.experimental.runBlocking
import org.apache.logging.log4j.LogManager
import java.io.IOException
import javax.inject.Inject

/**
 * The main entry point for the Command-Line Interface application.
 */
class ConsoleRunner
@Inject constructor(
    private val view: ConsoleView,
    private val mangaRepository: MangaRepository,
    private val mangaDownloader: MangaDownloader
) {

    private val logger = LogManager.getFormatterLogger("Console")

    /**
     * Executes the whole application.
     */
    fun start() = runBlocking {
        logger.info("Starting CLI application.")
        view.printWelcome()

        // Attempt to load the manga catalog.
        val catalog = try {
            mangaRepository.getAll().receive()
        } catch (ioe: IOException) {
            logger.fatal("Error while loading catalog", ioe)
            view.showErrorMessage(ioe.message ?: "Error while reading manga sources")
            return@runBlocking
        }

        // Abort with an error message if catalog is empty.
        if (catalog.isEmpty()) {
            logger.warn("Manga catalog is empty. maybe should be filled ?")
            view.reportEmptyCatalog()
            return@runBlocking
        }

        view.displayMangaList(catalog)

        // Loop until user give a correct manga id.
        var pickedSource: MangaSource? = null
        while (pickedSource == null) {
            val sourceId = view.askSourceId()
            if (sourceId < 0) return@runBlocking
            pickedSource = catalog.firstOrNull { it.id == sourceId }
        }

        // Get the range of chapter to be downloaded for that manga.
        val chapterRange = view.askChapterRange()

        // Download each chapter sequentially.
        // This may be better to download pages from multiple chapters at the same time,
        // but this makes it harder to display progress in CLI mode.
        for (chapter in chapterRange) {
            view.updateProgress(chapter)
            val result = mangaDownloader.loadChapterAsync(pickedSource, chapter).await()
            view.writeChapterResult(chapter, result.pages, result.error)
        }
    }

    /**
     * Perform termination tasks, such as cleaning-up resources
     * and waiting for user feedback before closing terminal window.
     */
    fun stop() {
        logger.info("CLI application terminated normally.")
        view.reportTerminated()
    }
}

fun main(args: Array<String>) {
    val dependencies = DaggerConsoleComponent.builder()
        .filenameProviderModule(FilenameProviderModule("pages"))
        .build()

    with(dependencies.console) {
        start()
        stop()
    }
}