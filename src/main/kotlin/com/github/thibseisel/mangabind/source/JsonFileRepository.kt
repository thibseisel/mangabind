package com.github.thibseisel.mangabind.source

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

/**
 * A manga source repository that read from and writes to a local JSON file.
 *
 * @param mapper An utility that maps JSON to Kotlin objects or vice-versa.
 * @param catalogFile a file where sources should be read from or written to.
 * If no such file exists, it will be created when writing to it.
 */
class JsonFileRepository
@Inject constructor(
    private val mapper: ObjectMapper,
    @Named("catalog") private val catalogFile: File
) : MangaRepository {

    private val sourceListType: CollectionType = mapper.typeFactory.constructCollectionType(
        List::class.java,
        MangaSource::class.java
    )

    /** Confines file read and writes to a single thread to prevent from writing and reading at the same time. */
    private val fileThread = newSingleThreadContext("FileAccessor")

    /** Maintains the list of loaded manga into memory in order to write it back to the file when it changes. */
    private val memoryCache = mutableListOf<MangaSource>()

    /** Broadcast file invalidations downto listeners. */
    private val invalidationBroadcaster = BroadcastChannel<Nothing?>(Channel.CONFLATED)

    override fun getAll(): ReceiveChannel<List<MangaSource>> = produce(capacity = Channel.CONFLATED) {
            // Initially send the result of querying the file.
            send(null)

            // Send further changes to the file everytime it is invalidated.
            val changesNotifier = invalidationBroadcaster.openSubscription()
            invokeOnClose { changesNotifier.cancel() }
            changesNotifier.consumeEach { send(null) }
        }.map {
            readSourcesFromFile()
        }

    override suspend fun save(manga: MangaSource) {
        memoryCache.removeIf { it.id == manga.id }
        memoryCache += manga
        updateFile()
    }

    override suspend fun delete(manga: MangaSource) {
        memoryCache.removeIf { it.id == manga.id }
        updateFile()
    }

    /**
     * Write the cached manga sources to the store file.
     */
    private suspend fun updateFile() = withContext(fileThread) {
        mapper.writeValue(catalogFile, memoryCache)
        // Notify observers that the file content has been invalidated.
        invalidationBroadcaster.send(null)
    }

    private suspend fun readSourcesFromFile(): List<MangaSource> = withContext(fileThread) {
        catalogFile.takeIf(File::exists)?.let { file ->
            try {
                mapper.readValue<List<MangaSource>>(file, sourceListType).also { readMangas ->
                    memoryCache.clear()
                    memoryCache.addAll(readMangas)
                }
            } catch (jpe: JsonParseException) {
                throw IOException("Cannot read manga catalog: file contains malformed JSON.")
            } catch (jme: JsonMappingException) {
                throw IOException("Cannot read manga catalog: file content cannot be interpreted as manga sources.")
            }
        } ?: emptyList()
    }
}