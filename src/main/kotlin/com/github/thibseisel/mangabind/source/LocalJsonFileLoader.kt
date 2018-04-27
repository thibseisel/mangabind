package com.github.thibseisel.mangabind.source

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class LocalJsonFileLoader
@Inject constructor(
    private val mapper: ObjectMapper,
    @Named("catalog") private val filepath: String
) : MangaRepository {

    private val sourceListType: CollectionType = mapper.typeFactory.constructCollectionType(
        List::class.java,
        MangaSource::class.java
    )

    override fun getAll(): List<MangaSource> {
        File(filepath).takeIf(File::exists)?.let {
            return try {
                mapper.readValue(it, sourceListType)
            } catch (jpe: JsonParseException) {
                throw IOException("Cannot read manga catalog: file contains malformed JSON.")
            } catch (jme: JsonMappingException) {
                throw IOException("Cannot read manga catalog: file content cannot be interpreted as manga sources.")
            }
        } ?: throw FileNotFoundException("Cannot read manga catalog: cannot find file $filepath")
    }

    override fun save(manga: MangaSource) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(manga: MangaSource) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}