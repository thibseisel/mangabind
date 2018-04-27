package com.github.thibseisel.mangabind.dagger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppModule {

    @Provides @Singleton
    fun providesJacksonMapper(): ObjectMapper = jacksonObjectMapper()

    @Provides @Singleton
    fun providesHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10L, TimeUnit.SECONDS)
        .readTimeout(30L, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun providesTranslationBundle(): ResourceBundle =
            ResourceBundle.getBundle("values/strings", Locale.getDefault())
}