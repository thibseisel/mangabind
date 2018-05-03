package com.github.thibseisel.mangabind.source

import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Provides the list of manga resources available to this application.
 * This interface abstracts away the origin of the manga catalog and the way to retrieve it.
 */
interface MangaRepository {

    /**
     * Load the whole list of available mangas resources.
     * This produces a channel whose elements are updates to the list of mangas.
     *
     * If an error occurs while loading the list, the channel will be closed with the error that occurred,
     * and its message should describe what happened to the user.
     *
     * @return The list of manga resources.
     */
    fun getAll(): ReceiveChannel<List<MangaSource>>

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