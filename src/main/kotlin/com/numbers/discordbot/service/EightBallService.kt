package com.numbers.discordbot.service

import com.numbers.discordbot.network.EightBallResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface EightBallService {

    @GET("https://8ball.delegator.com/magic/JSON/{question}")
    fun shake(@Path("question") question: String): Call<EightBallResponse>

    companion object {
        val Contrary = "Contrary"
        val Affirmative = "Affirmative"
        val Neutral = "Neutral"
    }

}