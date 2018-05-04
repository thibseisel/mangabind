package com.github.thibseisel.mangabind.packaging

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap

@Module
@Suppress("unused")
abstract class PackagingModule {

    @Binds @IntoMap
    @Key(Packager.Output.FOLDER)
    abstract fun bindsFolderPackager(folder: FolderPackager): Packager

    @Binds @IntoMap
    @Key(Packager.Output.CBZ)
    abstract fun bindsComicBookPackager(cbz: ComicBookPackager): Packager

    @MapKey
    annotation class Key(val value: Packager.Output)
}