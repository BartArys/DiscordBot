package com.numbers.discordbot

import com.google.inject.Guice
import com.numbers.discordbot.audio.MusicManagerMap
import com.numbers.discordbot.client.Init
import com.numbers.discordbot.dependency.CommandModule
import com.numbers.discordbot.dependency.ConcurrencyModule
import com.numbers.discordbot.dependency.HttpModule
import com.numbers.discordbot.dependency.PersistenceModule
import com.numbers.discordbot.filter.MesageEventListener
import com.numbers.discordbot.loader.CommandLoader
import com.numbers.discordbot.persistence.MongoDB

@Throws(Exception::class)
fun main(args: Array<String>)
{
    val client = Init.withToken().idle("shitty code simulator").login()

    ProcessBuilder(Init.mongoDbPath()).start()
    ProcessBuilder(Init.nodeServerPath().asList()).start()

    val db = MongoDB()

    val injector = Guice.createInjector(
            CommandModule(db, MusicManagerMap(), client),
            PersistenceModule(db),
            HttpModule(),
            ConcurrencyModule()
    )

    Runtime.getRuntime().addShutdownHook(Thread {
        client.invisible()
        client.connectedVoiceChannels.forEach { it.leave() }
    })

    val mel = MesageEventListener(injector)

    val classes = CommandLoader().getClasses(
            Command::class.java,
            "com.numbers.discordbot.commands"
    )

    ChronoService(directory = "com.numbers.discordbot.chrono", injector = injector)

    classes.forEach { mel.addCommand(it.getConstructor().newInstance()) }
    client.dispatcher.registerListener(mel)
}
