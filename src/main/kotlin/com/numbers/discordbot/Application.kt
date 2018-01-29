package com.numbers.discordbot

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.google.inject.*
import com.mongodb.async.client.MongoDatabase
import com.numbers.discordbot.action.*
import com.numbers.discordbot.action.music.*
import com.numbers.discordbot.action.music.playlist.*
import com.numbers.discordbot.action.permission.GrantPermissionAction
import com.numbers.discordbot.eventHandler.GuardedEventHandler
import com.numbers.discordbot.module.music.CachedMusicManager
import com.numbers.discordbot.module.music.MusicManager
import com.numbers.discordbot.network.DiscordEmojiResponse
import com.numbers.discordbot.network.EightBallResponse
import com.numbers.discordbot.network.deserializer.DiscordEmojiDeserializer
import com.numbers.discordbot.network.deserializer.DiscordEmojisDeserializer
import com.numbers.discordbot.network.deserializer.EightBallResponseDeserializer
import com.numbers.discordbot.service.*
import com.numbers.discordbot.trigger.LeaveTrigger
import com.numbers.discordbot.trigger.MessageTrigger
import com.numbers.discordbot.trigger.SelectSongTrigger
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import java.io.FileReader
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

val config = JsonParser().parse(FileReader("./bot.config.json")).asJsonObject!!

@Throws(Exception::class)
fun main(args: Array<String>) {
    val client = setUpClient()

    val injector = generateDependencyResolver(client)

    arrayOf(
            SelectSongTrigger::class,
            LeaveTrigger::class,
            MessageTrigger::class
    ).map { injector.getInstance(it.java) }.forEach { client.dispatcher.registerListener(it) }

    val actions = arrayOf(
            PleaseClapAction::class,
            SkipToAction::class,
            ReadImageAction::class,
            TagClaimAction::class,
            DisplayPlaylistsAction::class,
            DeletePlaylistAction::class,
            SetPrefixAction::class,
            SaveSongAction::class,
            QueueAction::class,
            PlayPlaylistAction::class,
            JoinAction::class,
            //DocumentAction::class,
            LeaveAction::class,
            WikiSearchAction::class,
            PlayAction::class,
            GatherCommand::class,
            SkipAction::class,
            PersonalityAction::class,
            EightBallAction::class,
            AddToPlaylistAction::class,
            RespectsAction::class,
            TagDeclaimAction::class,
            CreatePlaylistAction::class,
            NicknameAction::class,
            TagAction::class,
            EvalAction::class,
            CompileAction::class,
            ClapPermissionAction::class,
            BTCListenAction::class,
            GrantPermissionAction::class
    )

    GuardedEventHandler.injector = injector
    GuardedEventHandler.eventInjectors.add { it.message }
    GuardedEventHandler.eventInjectors.add { it.client }
    GuardedEventHandler.eventInjectors.add { it.channel }
    GuardedEventHandler.eventInjectors.add { it.author }
    GuardedEventHandler.eventInjectors.add { it.guild }
    GuardedEventHandler.eventInjectors.add { injector.getInstance(PersonalityManager::class.java).forUser(it.author) }
    GuardedEventHandler.eventInjectors.add { injector.getInstance(MusicManager::class.java).forGuild(it.guild) }
    GuardedEventHandler.eventInjectors.add { injector.getInstance(MusicManager::class.java).playListService }

    val listeners = actions.flatMap { GuardedEventHandler.toListener(it, injector) }

    listeners.forEach { client.dispatcher.registerListener(it) }
    client.login()
}

private fun setUpClient(): IDiscordClient {
    return ClientBuilder()
            .withToken(config["discord"].asJsonObject["token"].asString)
            .idle("shitty code simulator")
            .withRecommendedShardCount()
            .build()
}

private fun generateDependencyResolver(client: IDiscordClient): Injector {

    return Guice.createInjector(object : AbstractModule() {
        private val db = DBService()
        private val service = Executors.newSingleThreadScheduledExecutor { runnable -> Thread(runnable,"SERVICE THREAD") }
        private val gson = GsonBuilder()
                .registerTypeAdapter(EightBallResponse::class.java, EightBallResponseDeserializer())
                .registerTypeAdapter(DiscordEmojiResponse::class.java, DiscordEmojiDeserializer())
                .registerTypeAdapter(object : TypeToken<List<DiscordEmojiResponse>>() {}.type, DiscordEmojisDeserializer())
                .registerTypeAdapter(Map::class.java, BTCInfoDeserializer())
                .registerTypeAdapter(WikiSearchResult::class.java, WikiSearchDeserializer())
                .create()

        private val retrofit = Retrofit.Builder().baseUrl("https://www.google.com").addConverterFactory(GsonConverterFactory.create(gson)).build()

        override fun configure() {
            bind(com.numbers.discordbot.module.music.MusicManager::class.java).to(CachedMusicManager::class.java)
            bind(WikiSearchService::class.java).toInstance(retrofit.create(WikiSearchService::class.java))
            bind(EightBallService::class.java).toInstance(retrofit.create(EightBallService::class.java))
            bind(DiscordEmojiService::class.java).toInstance(retrofit.create(DiscordEmojiService::class.java))
            bind(BTCService::class.java).toProvider(Provider { retrofit.create(BTCService::class.java) })
            bind(ScheduledExecutorService::class.java).toInstance(service)
            bind(IDiscordClient::class.java).toInstance(client)
        }

        @Provides
        private fun getDB() : MongoDatabase = db.mongoDatabase
    })
}
