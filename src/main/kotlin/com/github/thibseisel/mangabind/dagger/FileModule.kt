package com.github.thibseisel.mangabind.dagger

import com.github.thibseisel.mangabind.source.JsonFileRepository
import com.github.thibseisel.mangabind.source.MangaRepository
import dagger.Binds
import dagger.Module

@Module(includes = [FilenameProviderModule::class])
abstract class FileModule {

    @Binds abstract fun bindsSourceLoader(impl: JsonFileRepository): MangaRepository
}