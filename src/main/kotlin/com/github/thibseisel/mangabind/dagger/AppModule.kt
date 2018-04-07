package com.github.thibseisel.mangabind.dagger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream

@Module
object AppModule {

    @JvmStatic
    @Provides fun providesJacksonMapper(): ObjectMapper = jacksonObjectMapper()

    @JvmStatic
    @Provides fun providesHttpClient(): OkHttpClient = OkHttpClient()

    @JvmStatic
    @Provides fun providesStandardInput(): BufferedReader = BufferedReader(InputStreamReader(System.`in`))

    @JvmStatic
    @Provides fun providesStandardOutput(): PrintStream = System.out
}