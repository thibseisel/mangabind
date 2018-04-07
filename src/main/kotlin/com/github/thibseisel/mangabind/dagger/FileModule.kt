package com.github.thibseisel.mangabind.dagger

import com.github.thibseisel.mangabind.source.LocalJsonCatalogLoader
import com.github.thibseisel.mangabind.source.SourceLoader
import dagger.Binds
import dagger.Module

@Module(includes = [FilenameProviderModule::class])
abstract class FileModule {

    @Binds abstract fun bindsSourceLoader(localImpl: LocalJsonCatalogLoader): SourceLoader
}