package com.github.thibseisel.mangabind.cli

import dagger.Module
import dagger.Provides
import java.io.BufferedReader
import java.io.PrintStream
import javax.inject.Singleton

/**
 * A Dagger module that describes how to inject console-related features, such as standard streams.
 * Abstracting away input and output allows tests to simulate user input and check for output
 * in console dedicated classes.
 */
@Module
class ConsoleModule {

    @Provides @Singleton
    fun providesStandardInput(): BufferedReader = System.`in`.bufferedReader()

    @Provides @Singleton
    fun providesStandardOutput(): PrintStream = System.out
}