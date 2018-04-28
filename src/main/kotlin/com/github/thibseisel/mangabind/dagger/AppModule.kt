package com.github.thibseisel.mangabind.dagger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job
import okhttp3.OkHttpClient
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * A Dagger module describing how to provides instances of application general utilities.
 */
@Module
class AppModule {

    /**
     * Provides a facility to convert JSON to object instances and vice-versa.
     */
    @Provides @Singleton
    fun providesJacksonMapper(): ObjectMapper = jacksonObjectMapper()

    /**
     * Provides a thread safe HTTP client to download pages.
     */
    @Provides @Singleton
    fun providesHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10L, TimeUnit.SECONDS)
        .readTimeout(30L, TimeUnit.SECONDS)
        .build()

    /**
     * Provides a bundle mapping keys to strings translated in the user's default system language.
     */
    @Provides @Singleton
    fun providesTranslationBundle(): ResourceBundle =
            ResourceBundle.getBundle("values/strings", Locale.getDefault())

    /**
     * Provides a Job to be used as the parent of all coroutines that should be completed
     * before the application process quits.
     */
    @Provides @Singleton
    fun providesParentJob() = Job()
}