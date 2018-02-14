package com.numbers.discordbot.service

import com.numbers.discordbot.network.DiscordEmojiResponse
import retrofit2.Call
import retrofit2.http.GET

interface DiscordEmojiService {

    @GET("https://discordemoji.com/api")
    fun getEmojis(): Call<List<DiscordEmojiResponse>>

}