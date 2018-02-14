package com.numbers.discordbot

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.google.inject.Provider
import com.mongodb.async.client.MongoDatabase
import com.numbers.discordbot.dsl.*
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
                    .registerTypeAdapter(EightBallResponse::class.java, EightBallResponseDeserializer())
                    .registerTypeAdapter(DiscordEmojiResponse::class.java, DiscordEmojiDeserializer())
                    .registerTypeAdapter(object : TypeToken<List<DiscordEmojiResponse>>() {}.type, DiscordEmojisDeserializer())
                    .registerTypeAdapter(object : TypeToken<Map<String, BTCInfo>>(){}.type, BTCInfoDeserializer())
                    .registerTypeAdapter(WikiSearchResult::class.java, WikiSearchDeserializer())
                    .create()
            inject(gson)

            val retrofit = Retrofit.Builder().baseUrl("https://www.google.com").addConverterFactory(GsonConverterFactory.create(gson)).build()

            inject(retrofit.create(WikiSearchService::class.java))
            inject(retrofit.create(EightBallService::class.java))
            inject(Executors.newSingleThreadScheduledExecutor { runnable -> Thread(runnable,"SERVICE THREAD") })
            inject(retrofit.create(BTCService::class.java))
        }
    }

    runBlocking {
        val client =  setup().await()
        val ready = IListener<ReadyEvent> {
            val now = System.currentTimeMillis()
            it.client.applicationOwner.orCreatePMChannel.sendMessage("init took ${Duration.ofMillis(now - start).format()}")
        }
        client.dispatcher.registerListener(ready)
        client.login()
    }

    /*
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
    GuardedEventHandler.eventInjectors.add { injector.getInstance(MusicManager::class.java).playerForGuild(it.guild) }
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
    })*/
}
