package com.github.thibseisel.mangabind.dagger

import com.github.thibseisel.mangabind.Mangabind
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    FileModule::class
])
interface AppComponent {
    val mangabind: Mangabind
}