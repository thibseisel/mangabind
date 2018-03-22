package com.github.thibseisel.mangabind

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

fun main(args: Array<String>) = runBlocking {

    val mangaSources = loadFromCatalog("mangasource.json")
    val tgSource = mangaSources.firstOrNull() ?: return@runBlocking

    for (manga in mangaSources) {
        println("""${manga.id}. ${manga.title}
            |   baseUrl: ${manga.baseUrl}
            |   singlePages: ${manga.singlePages}
            |   doublePages: ${manga.doublePages}
        """.trimMargin())
    }

    var chapterRange: IntRange?
    do {
        chapterRange = readChapterRange()
    } while (chapterRange == null)

    val results = ArrayList<Deferred<LoadResult>>()
    for (chapter in chapterRange) {
        results += async<LoadResult> {
            LoadResult(chapter, isSuccessful = true)
        }
    }

    results.map { it.await() }.forEach {
        if (it.isSuccessful) {
            System.out.println("Successfully downloaded chapter ${it.chapter}")
        } else {
            System.err.println("Error while loading chapter ${it.chapter}")
        }
    }
}

private fun loadFromCatalog(filename: String): List<MangaSource> {
    val mapper = ObjectMapper()
    val catalogIs = Thread.currentThread().contextClassLoader.getResourceAsStream(filename)
    return mapper.readValue(catalogIs,
            mapper.typeFactory.constructCollectionType(List::class.java, MangaSource::class.java)
    )
}

private val reRange = Regex("^\\d{1,3}-\\d{1,3}$")

private fun readChapterRange(): IntRange? {
    println("Range of chapters to download (format: X-Y) > ")
    val input = readLine() ?: return IntRange.EMPTY
    if (!input.matches(reRange)) return null

    val (startChapter, endChapter) = input.split('-').map { it.trim().toInt() }
    return startChapter..endChapter
}

class LoadResult(
        val chapter: Int,
        val isSuccessful: Boolean,
        val error: String? = null
)