package com.github.thibseisel.mangabind.source

import java.io.IOException

/**
 * Provides the list of manga resources available to this application.
 * This interface abstracts away the origin of the manga catalog and the way to retrieve it.
 */
interface MangaRepository {

    /**
     * Load the whole list of available mangas resources.
     * Depending on the implementation, this could be a long-running operation
     * and therefore should preferably be run on a worker thread.
     *
     * If an error occurs while loading the list, an [IOException] is thrown,
     * with its message describing what happened to the user.
     *
     * @return The list of manga resources.
     */
    fun getAll(): List<MangaSource>

    /**
     * Save a manga source into the repository.
     * If a manga source with the same id already exists, it is replaced without merging.
     * Otherwise, a new manga source will be added to the repository.
     *
     * @param manga The manga source to save to the repository.
     */
    fun save(manga: MangaSource)

    /**
     * Deletes an manga source from the repository.
     * This will silently fail if not such source exists.
     *
     * @param manga The manga source to delete.
     */
    fun delete(manga: MangaSource)
}