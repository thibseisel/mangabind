package com.github.thibseisel.mangabind.source

import kotlinx.coroutines.experimental.channels.ReceiveChannel

interface MangaRepository {

    fun getAll(): ReceiveChannel<List<MangaSource>>
    fun save(manga: MangaSource)
    fun delete(manga: MangaSource)
}
