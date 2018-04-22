package com.numbers.discordbot.extensions

import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Converter.Factory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.experimental.suspendCoroutine

suspend fun <T> Call<T>.await(): Response<T> = suspendCoroutine {
    it.resume(this.execute())
}

inline fun retrofit(block: RetroFitBuilder.() -> Unit): Retrofit {
    val config = RetroFitBuilder()
    config.apply(block)

    val builder = Retrofit.Builder().baseUrl(config.baseUrl!!)
    config.converters.forEach { builder.addConverterFactory(it) }
    return builder.build()
}

val Gson.asConverterFactory: GsonConverterFactory inline get() = GsonConverterFactory.create(this)

data class RetroFitBuilder(
        var baseUrl: String? = null,
        val converters: MutableList<Factory> = mutableListOf()
)

operator fun MutableList<Factory>.plusAssign(factory: Factory) {
    this.add(factory)
}

inline fun <reified T> Retrofit.create(): T = this.create(T::class.java)