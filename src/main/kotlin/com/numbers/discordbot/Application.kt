package com.numbers.discordbot

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.numbers.discordbot.arguments.prefix
import com.numbers.discordbot.extensions.asConverterFactory
import com.numbers.discordbot.extensions.create
import com.numbers.discordbot.extensions.readAsBrowser
import com.numbers.discordbot.extensions.retrofit
import com.numbers.discordbot.module.music.CachedMusicManager
import com.numbers.discordbot.module.music.MusicManager
import com.numbers.discordbot.service.EightBallService
import com.numbers.discordbot.service.InspirationService
import com.numbers.discordbot.service.RedditInitService
import com.numbers.discordbot.service.WikiSearchService
import com.numbers.discordbot.service.discordservices.*
import com.numbers.disko.inject
import com.numbers.disko.setup
import com.numbers.disko.*
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.ReadyEvent
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.io.FileReader
import java.net.URL
import java.time.Duration

lateinit var trayIcon: TrayIcon
val config = JsonParser().parse(FileReader("./bot.config.json")).asJsonObject!!

@Throws(Exception::class)
fun main(args: Array<String>) {
    System.setProperty("idea.io.use.fallback", "true")

    val start = System.currentTimeMillis()

    val setup = setup {
        commandPackages += "com.numbers.discordbot.commands.defaultCommands"

        token = config["discord"].asJsonObject["token"].asString

        if(config.has("reddit")){
            commandPackages += "com.numbers.discordbot.commands.redditCommands"
            inject { inject(RedditInitService().reddit) }
        }

        arguments {
            argumentToken = "$"
            forToken('£', prefix)
            forArgument("u",    userMention("user"))
            forArgument("vc",   voiceChannel("voiceChannel"))
            forArgument("tc",   textChannelMention("textChannel"))
            forArgument("url",  url("url"))
            forArgument("i",    integer("number"))
            forArgument("i+",   positiveInteger("number"))
            forArgument("i^0+", strictPositiveInteger("number"))
            forArgument("i-",   negativeInteger("number"))
            forArgument("i^0-", strictNegativeInteger("number"))
            forArgument("bot",  appMention)
        }

        inject {
            injectSupplier<MusicManager, CachedMusicManager>()

            injectContextually { it.services<MusicManager>().playerForGuild(it.guild!!) }

            val gson = GsonBuilder().setLenient().create()
            inject(gson)

            val retrofit = retrofit {
                baseUrl = "https://www.google.com"
                converters += gson.asConverterFactory
            }

            val discordRetrofit = retrofit {
                baseUrl = "http://localhost:6969"
                converters += gson.asConverterFactory
            }

            with(discordRetrofit) {
                inject(create<PlaylistWebService>())
                inject(create<ReactionWebService>())
            }

            injectSupplier<PlaylistService, InternalPlaylistService>()
            injectSupplier<ReactionService, InternalReactionService>()

            val prefixWebService = discordRetrofit.create<PrefixWebService>()
            inject(prefixWebService)

            val prefixService = InternalPrefixService(gson = gson, webService = prefixWebService, wsUrl = "ws://localhost:6969/prefixes")
            inject(prefixService as PrefixService)

            with(retrofit) {
                inject(create<InspirationService>())
                inject(create<WikiSearchService>())
                inject(create<EightBallService>())
            }
        }
    }

    //setup += JsonTranspiler.generateFromJson("commands.json")

    val client = setup()
    val ready = IListener<ReadyEvent> {
        val now = System.currentTimeMillis()
        val tray = SystemTray.getSystemTray()
        val icon = readAsBrowser(URL(client.ourUser.avatarURL.replace("webp", "png")))
        trayIcon = TrayIcon(icon)
        trayIcon.isImageAutoSize = true
        trayIcon.popupMenu = PopupMenu()
        val closeItem = MenuItem()
        closeItem.addActionListener { System.exit(0) }
        closeItem.label = "close"
        trayIcon.popupMenu.add(closeItem)
        tray.add(trayIcon)
        trayIcon.displayMessage("Discord Bot", "init took ${Duration.ofMillis(now - start).toMillis()} ms", TrayIcon.MessageType.NONE)
    }
    client.dispatcher.registerTemporaryListener(ready)
    client.login()
}