package com.github.thibseisel.mangabind.source

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.annotations.TestOnly
import java.io.FileNotFoundException
import java.io.IOException

private const val RES_FILENAME = "mangasource.json"

/**
 * Loads manga resource list from the JSON catalog bundled with the application.
 *
 * @constructor
 * Creates an instance of a loader that fetches from a given resource file bundled with the application.
 * This should only be used for test purposes.
 * For production, the parameter-less constructor should be used instead.
 *
 * @param filename The name of a JSON resource file bundled with the application from which manga should be read.
 */
class LocalJsonCatalogLoader
@TestOnly constructor(
        private val filename: String
): SourceLoader {

    /**
     * Creates an instance of a loader that fetches from the bundled catalog file.
     */
    constructor() : this(RES_FILENAME)

    private val mapper: ObjectMapper = jacksonObjectMapper()

    private val sourceListType: CollectionType = mapper.typeFactory.constructCollectionType(
            List::class.java,
            MangaSource::class.java
    )

    @Throws(IOException::class)
    override fun loadAll(): List<MangaSource> {
        Thread.currentThread().contextClassLoader.getResourceAsStream(filename)?.let {
            return try {
                mapper.readValue(it, sourceListType)
            } catch (jpe: JsonParseException) {
                throw IOException("Cannot read manga catalog: file contains malformed JSON.")
            } catch (jme: JsonMappingException) {
                throw IOException("Cannot read manga catalog: file content cannot be interpreted as manga sources.")
            }

        } ?: throw FileNotFoundException("Cannot read manga catalog: file not found.")
    }
}