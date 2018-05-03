package com.github.thibseisel.mangabind.dagger

import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Named

@Module
class FileProviderModule {

    @Provides
    @Named("catalog") fun providesCatalogFile(): File = File("mangasource.json")

    @Provides
    @Named("tmpDir") fun providesOutputDirectory(): File = createTempDir("images").apply {
        deleteOnExit()
    }
}