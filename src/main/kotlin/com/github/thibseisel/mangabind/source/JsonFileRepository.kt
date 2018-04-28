package com.github.thibseisel.mangabind.source

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

/**
 * A manga source repository that read from and writes to a local JSON file.
 *
 * @param parentJob The job that will be used as the parent for all coroutines,
 * so that it can wait for its children to complete.
 * @param mapper An utility that maps JSON to Kotlin objects or vice-versa.
 * @param catalogPath The location of a file where sources should be read from or written to.
 * If no such file exists, it will be created.
 */
class JsonFileRepository
@Inject constructor(
    private val parentJob: Job,
    private val mapper: ObjectMapper,
    @Named("catalog") catalogPath: String
) : MangaRepository {

    private val sourceListType: CollectionType = mapper.typeFactory.constructCollectionType(
        List::class.java,
        MangaSource::class.java
    )

    /**
     * Confines file read and writes to a single thread to prevent from writing and reading at the same time,
     * resulting in data losses.
     */
    private val fileThread = newSingleThreadContext("FileAccessor")

    private val catalogFile = File(catalogPath)
    private val memoryCache = mutableListOf<MangaSource>()

    private var outChannel: Channel<List<MangaSource>>? = null

    override fun getAll(): ReceiveChannel<List<MangaSource>> {
        outChannel?.close()
        return Channel<List<MangaSource>>(capacity = Channel.CONFLATED).also {
            outChannel = it
            loadFromFileAsync()
        }
    }

    private fun loadFromFileAsync() = launch(fileThread, parent = parentJob) {
        catalogFile.takeIf(File::exists)?.let {
            try {
                val sourcesFromFile = mapper.readValue<List<MangaSource>>(it, sourceListType)
                memoryCache.clear()
                memoryCache += sourcesFromFile
                outChannel?.send(sourcesFromFile)

            } catch (jpe: JsonParseException) {
                throw IOException("Cannot read manga catalog: file contains malformed JSON.")
            } catch (jme: JsonMappingException) {
                throw IOException("Cannot read manga catalog: file content cannot be interpreted as manga sources.")
            }
        }
    }

    override fun save(manga: MangaSource) {
        memoryCache.removeIf { it.id == manga.id }
        memoryCache += manga
        updateFileAsync()
    }

    override fun delete(manga: MangaSource) {
        memoryCache.removeIf { it.id == manga.id }
        updateFileAsync()
    }

    /**
     * Write the cached manga sources to the store file.
     * This is guaranteed to not occur while that file is being read.
     */
    private fun updateFileAsync() = launch(fileThread, parent = parentJob) {
        mapper.writeValue(catalogFile, memoryCache)
    }

}