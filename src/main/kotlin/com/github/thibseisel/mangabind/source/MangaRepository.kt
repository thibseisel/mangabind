package com.github.thibseisel.mangabind.source

interface MangaRepository {

    fun getAll(): List<MangaSource>
    fun save(manga: MangaSource)
    fun delete(manga: MangaSource)
}
