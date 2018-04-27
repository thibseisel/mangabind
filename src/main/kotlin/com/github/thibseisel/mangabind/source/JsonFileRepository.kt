package com.github.thibseisel.mangabind.source

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class JsonFileRepository
@Inject constructor(
    private val mapper: ObjectMapper,
    @Named("catalog") private val filepath: String
) : MangaRepository {

    private val sourceListType: CollectionType = mapper.typeFactory.constructCollectionType(
        List::class.java,
        MangaSource::class.java
    )

    private val catalogFile = File(filepath)
    private val memoryCache = mutableListOf<MangaSource>()

    override fun getAll(): List<MangaSource> {
        return File(filepath).takeIf(File::exists)?.let {
            try {
                mapper.readValue<List<MangaSource>>(it, sourceListType).also {
                    memoryCache.clear()
                    memoryCache += it
                }
            } catch (jpe: JsonParseException) {
                throw IOException("Cannot read manga catalog: file contains malformed JSON.")
            } catch (jme: JsonMappingException) {
                throw IOException("Cannot read manga catalog: file content cannot be interpreted as manga sources.")
            }
        } ?: emptyList()
    }

    override fun save(manga: MangaSource) {
        memoryCache.removeIf { it.id == manga.id }
        memoryCache += manga
        mapper.writeValue(catalogFile, memoryCache)
    }

    override fun delete(manga: MangaSource) {
        memoryCache.removeIf { it.id == manga.id }
        mapper.writeValue(catalogFile, memoryCache)
    }
}