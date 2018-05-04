package com.github.thibseisel.mangabind.cli

import com.github.thibseisel.mangabind.MangaDownloader
import com.github.thibseisel.mangabind.clear
import com.github.thibseisel.mangabind.packaging.Packager
import com.github.thibseisel.mangabind.source.MangaRepository
import com.github.thibseisel.mangabind.source.MangaSource
import kotlinx.coroutines.experimental.runBlocking
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

/**
 * The main entry point for the Command-Line Interface application.
 */
class ConsoleRunner
@Inject constructor(
        private val view: ConsoleView,
        private val mangaRepository: MangaRepository,
        private val mangaDownloader: MangaDownloader,
        private val packagers: Map<Packager.Output, @JvmSuppressWildcards Packager>,
        @Named("tmpDir") private val imagesDir: File
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

        // Delete images from previous downloads before use.
        imagesDir.clear()

        // Download each chapter sequentially.
        // This may be better to download pages from multiple chapters at the same time,
        // but this makes it harder to display progress in CLI mode.
        for (chapter in chapterRange) {
            view.updateProgress(chapter)
            val result = mangaDownloader.loadChapterAsync(pickedSource, chapter).await()
            view.writeChapterResult(chapter, result.pages, result.error)
        }

        // When all pages have been downloaded, ask how to package downloaded images.
        val packageToCbz = view.askShouldPackageCbz()
        val packager = packagers[if (packageToCbz) Packager.Output.CBZ else Packager.Output.FOLDER]
                ?: throw IllegalStateException("Neither packager is available")

        val packagedPath = "%s/mangas/%s_%d_%d".format(
                System.getProperty("user.home"),
                pickedSource.title.replace("\\s", "_"),
                chapterRange.first,
                chapterRange.last
        )

        try {
            val output = packager.create(packagedPath)
            view.reportPackagingSuccess(output)
        } catch (e: Exception) {
            logger.error("Error while packaging downloaded images", e)
            view.showErrorMessage(e.message ?: "Error while packaging downloaded images")
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
    val dependencies = DaggerConsoleComponent.create()

    with(dependencies.console) {
        start()
        stop()
    }
}