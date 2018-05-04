package com.github.thibseisel.mangabind.cli

import com.github.thibseisel.mangabind.dagger.CommonModule
import dagger.Component
import javax.inject.Singleton

/**
 * The main Dagger Component for the Command Line Interface entry point.
 * It is bound to the whole application scope and should be initialized as soon as the application starts.
 */
@Singleton
@Component(modules = [
    CommonModule::class,
    ConsoleModule::class
])
interface ConsoleComponent {

    /**
     * Produces an instance of the main entry-point for the console application
     * with all its dependencies being injected.
     */
    val console: ConsoleRunner
}