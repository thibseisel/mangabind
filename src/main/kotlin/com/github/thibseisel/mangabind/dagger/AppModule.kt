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
object AppModule {

    @JvmStatic
    @Provides @Singleton
    fun providesJacksonMapper(): ObjectMapper = jacksonObjectMapper()

    @JvmStatic
    @Provides @Singleton
    fun providesHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10L, TimeUnit.SECONDS)
        .readTimeout(30L, TimeUnit.SECONDS)
        .build()

    @JvmStatic
    @Provides @Singleton
    fun providesStandardInput(): BufferedReader = BufferedReader(InputStreamReader(System.`in`))

    @JvmStatic
    @Provides @Singleton
    fun providesStandardOutput(): PrintStream = System.out
}