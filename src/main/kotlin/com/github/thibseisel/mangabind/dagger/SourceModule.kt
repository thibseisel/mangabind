package com.github.thibseisel.mangabind.dagger

import com.github.thibseisel.mangabind.source.JsonFileRepository
import com.github.thibseisel.mangabind.source.MangaRepository
import dagger.Binds
import dagger.Module

/**
 * A Dagger module for manga sources-related dependencies.
 */
@Suppress("unused")
@Module
abstract class SourceModule {

    @Binds abstract fun bindsSourceLoader(impl: JsonFileRepository): MangaRepository
}