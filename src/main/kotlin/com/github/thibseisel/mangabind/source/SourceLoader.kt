package com.github.thibseisel.mangabind.source

import com.github.thibseisel.mangabind.MangaSource
import java.io.IOException

/**
 * Provides the list of manga resources available to this application.
 * This interface abstracts the origin of the manga catalog and the way to retrieve it.
 */
interface SourceLoader {

    /**
     * Load the whole list of available mangas resources.
     * Depending on the implementation, this could be a long-running operation
     * and therefore should preferably be run on a worker thread.
     *
     * If an error occurs while loading the list, an [IOException] is thrown,
     * with its message describing what happened to the user.
     *
     * @return The list of manga resources.
     * @throws IOException If something went wrong while loading the resources list.
     */
    @Throws(IOException::class)
    fun loadAll(): List<MangaSource>
}