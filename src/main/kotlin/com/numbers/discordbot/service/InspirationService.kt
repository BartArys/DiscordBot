package com.numbers.discordbot.service

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface InspirationService {
    @GET("http://inspirobot.me/api?generate=true")
    fun generateQuote(): Call<ResponseBody>

}