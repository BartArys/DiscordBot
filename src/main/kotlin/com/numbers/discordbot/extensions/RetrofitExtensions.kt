package com.numbers.discordbot.extensions

import retrofit2.Call
import retrofit2.Response
import kotlin.coroutines.experimental.suspendCoroutine

suspend fun<T> Call<T>.await() : Response<T> = suspendCoroutine {
    it.resume(this.execute())
}