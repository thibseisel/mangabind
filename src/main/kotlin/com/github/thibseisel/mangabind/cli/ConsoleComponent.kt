package com.github.thibseisel.mangabind.cli

import com.github.thibseisel.mangabind.Mangabind
import com.github.thibseisel.mangabind.dagger.AppModule
import com.github.thibseisel.mangabind.dagger.FileModule
import dagger.Component
import javax.inject.Singleton

/**
 * The main Dagger Component for the Command Line Interface entry point.
 * It is bound to the whole application scope and should be initialized as soon as the application starts.
 */
@Singleton
@Component(modules = [
    AppModule::class,
    ConsoleModule::class,
    FileModule::class
])
interface ConsoleComponent {
    val mangabind: Mangabind
}