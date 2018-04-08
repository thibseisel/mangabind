package com.github.thibseisel.mangabind.dagger

import com.github.thibseisel.mangabind.source.JsonResourceLoader
import com.github.thibseisel.mangabind.source.LocalJsonFileLoader
import com.github.thibseisel.mangabind.source.SourceLoader
import dagger.Binds
import dagger.Module

@Module(includes = [FilenameProviderModule::class])
abstract class FileModule {

    @Binds abstract fun bindsSourceLoader(impl: LocalJsonFileLoader): SourceLoader
}