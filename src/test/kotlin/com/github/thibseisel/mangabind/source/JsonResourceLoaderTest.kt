package com.github.thibseisel.mangabind.source

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.FileNotFoundException
import java.io.IOException

class JsonResourceLoaderTest {

    private val mapper = jacksonObjectMapper()

    @Test(expected = FileNotFoundException::class)
    fun whenFileDoesNotExists_throwsIOException() {
        val loader = JsonResourceLoader(mapper, "unavailable_file.json")
        loader.loadAll()
    }

    @Test
    fun whenReadingValidEmptyFile_returnsEmptyList() {
        val loader = JsonResourceLoader(mapper, "empty.json")
        val sources = loader.loadAll()

        assertTrue(sources.isEmpty())
    }

    @Test(expected = IOException::class)
    fun whenReadingMalformedJson_throwsIOException() {
        val loader = JsonResourceLoader(mapper, "malformed.json")
        loader.loadAll()
    }

    @Test(expected = IOException::class)
    fun whenReadingInvalidData_throwsIOException() {
        val loader = JsonResourceLoader(mapper, "invalid.json")
        loader.loadAll()
    }
}