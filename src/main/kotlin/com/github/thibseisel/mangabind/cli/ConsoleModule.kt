package com.github.thibseisel.mangabind.cli

import dagger.Module
import dagger.Provides
import java.io.BufferedReader
import java.io.PrintStream
import javax.inject.Singleton

/**
 * A Dagger module that describes how to provide console-specific dependencies.
 *
 * Abstracting away input and output allows tests to simulate user input and check for output
 * in console dedicated classes.
 */
@Module
class ConsoleModule {

    /**
     * provides a reader of the stream that should be used as the standard input.
     */
    @Provides @Singleton
    fun providesStandardInput(): BufferedReader = System.`in`.bufferedReader()

    /**
     * Provides the stream that should act as the standard output for the CLI application.
     */
    @Provides @Singleton
    fun providesStandardOutput(): PrintStream = System.out
}