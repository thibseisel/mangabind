package com.github.thibseisel.mangabind.source

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.FileNotFoundException
import java.io.IOException

class LocalJsonCatalogLoaderTest {

    @Test(expected = FileNotFoundException::class)
    fun whenFileDoesNotExists_throwsIOException() {
        val loader = LocalJsonCatalogLoader("unavailable_file.json")
        loader.loadAll()
    }

    @Test
    fun whenReadingValidEmptyFile_returnsEmptyList() {
        val loader = LocalJsonCatalogLoader("empty.json")
        val sources = loader.loadAll()

        assertTrue(sources.isEmpty())
    }

    @Test(expected = IOException::class)
    fun whenReadingMalformedJson_throwsIOException() {
        val loader = LocalJsonCatalogLoader("malformed.json")
        loader.loadAll()
    }

    @Test(expected = IOException::class)
    fun whenReadingInvalidData_throwsIOException() {
        val loader = LocalJsonCatalogLoader("invalid.json")
        loader.loadAll()
    }
}