package com.numbers.discordbot.client

import sx.blah.discord.api.ClientBuilder

class Init {

    companion object {

        @JvmStatic
        fun withToken(): ClientBuilder = ClientBuilder().withToken("MjQ5ODI2MzMyMTU2ODg3MDQw.DBlhuQ.yivVqDaejV-dHUo5zRflT8U3uVM")

        @JvmStatic
        fun OpenWeatherKey(): String = "bc1fe6b4b789099900652a34666ecbe4"

        @JvmStatic
        fun mongoDbPath(): String = "\"C:\\Program Files\\MongoDB\\Server\\3.4\\bin\\mongod.exe\""

        @JvmStatic
        fun nodeServerPath(): Array<String> = arrayOf("node", "D:\\nodeProjects\\nodeRedditPlaylistService\\index.js")
    }

}