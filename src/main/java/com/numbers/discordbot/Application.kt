package com.numbers.discordbot

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.google.inject.Provider
import com.mongodb.async.client.MongoDatabase
import com.numbers.discordbot.dsl.*
import com.numbers.discordbot.extensions.*
import com.numbers.discordbot.module.music.CachedMusicManager
import com.numbers.discordbot.module.music.MusicManager
import com.numbers.discordbot.module.music.format
import com.numbers.discordbot.network.DiscordEmojiResponse
import com.numbers.discordbot.network.EightBallResponse
import com.numbers.discordbot.network.deserializer.DiscordEmojiDeserializer
import com.numbers.discordbot.network.deserializer.DiscordEmojisDeserializer
import com.numbers.discordbot.network.deserializer.EightBallResponseDeserializer
import com.numbers.discordbot.service.*
import kotlinx.coroutines.experimental.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.ReadyEvent
import java.io.FileReader
import java.time.Duration
import java.util.concurrent.Executors

val config = JsonParser().parse(FileReader("./bot.config.json")).asJsonObject!!

@Throws(Exception::class)
fun main(args: Array<String>) {

    val start = System.currentTimeMillis()

    val setup = setup {
        commandPackages += "com.numbers.discordbot.commands"
        token = config["discord"].asJsonObject["token"].asString

        arguments {
            argumentToken = "$"
            forToken('£') { prefix }
            forArgument("u") { userMention("user") }
            forArgument("vc") { voiceChannel("voiceChannel") }
            forArgument("tc") { textChannelMention("textChannel") }
            forArgument("url") { url("url") }
            forArgument("i") { integer("number") }
            forArgument("i+") { positiveInteger("number") }
            forArgument("i^0+") { strictPositiveInteger("number") }
            forArgument("i-") { negativeInteger("number") }
            forArgument("i^0-") { strictNegativeInteger("number") }
            forArgument("bot") { appMention }
        }

        inject {
            injectSupplier<MusicManager, CachedMusicManager>()

            injectContextually { it.services<MusicManager>().playerForGuild(it.guild) }
            injectContextually { it.services<MusicManager>().playListService }
            injectContextually { it.services<PersonalityManager>().forUser(it.author) }

            injectSupplier<MongoDatabase>(Provider { DBService.database })

            val gson = GsonBuilder()
                    .registerTypeAdapter<EightBallResponse>(EightBallResponseDeserializer())
                    .registerTypeAdapter<Map<String, BTCInfo>>(BTCInfoDeserializer())
                    .registerTypeAdapter<WikiSearchResult>(WikiSearchDeserializer())
                    .create()
            inject(gson)

            val retrofit = retrofit {
                baseUrl = "https://www.google.com"
                converters += gson.asConverterFactory
            }

            with(retrofit) {
                inject(create<WikiSearchService>())
                inject(create<EightBallService>())
                inject(create<BTCService>())
            }

        }
    }

    runBlocking {
        val client = setup().await()
        val ready = IListener<ReadyEvent> {
            val now = System.currentTimeMillis()
            it.client.applicationOwner.orCreatePMChannel.sendMessage("init took ${Duration.ofMillis(now - start).format()}")
        }
        client.dispatcher.registerListener(ready)
        client.login()
    }
}