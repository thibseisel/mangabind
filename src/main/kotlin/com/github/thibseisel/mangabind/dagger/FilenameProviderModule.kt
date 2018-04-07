package com.github.thibseisel.mangabind.dagger

import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class FilenameProviderModule(
    private val outDir: String
) {

    @Provides
    @Named("catalog") fun providesCatalogFilename(): String = "mangasource.json"

    @Provides
    @Named("outputDir") fun providesOutputDirectory(): String = outDir
}