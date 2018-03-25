package com.numbers.discordbot.commands

import com.numbers.discordbot.dsl.commands
import com.numbers.discordbot.dsl.invoke
import com.numbers.discordbot.dsl.strictPositiveInteger
import com.numbers.discordbot.dsl.words
import com.numbers.discordbot.extensions.add
import com.numbers.discordbot.extensions.search
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.module.music.MusicPlayerMessageStore
import com.numbers.discordbot.module.music.toSelectScreen
import com.numbers.discordbot.toScreen
import java.awt.Color

fun musicCommands() = commands {

    command("£p {url}|{search}")
    command("£ play {url}|{search}"){

        arguments(words("search"))

        execute {
            MusicPlayerMessageStore(guild!!.longID) {
                respondScreen("building player...", services<MusicPlayer>().toScreen(author)).await()
            }

            val player = services<MusicPlayer>()

            val search = args<String>("url") ?: "ytsearch:${args<String>("search")}"

            val results = player.search(search, author).toList()
            when (results.count()) {
                0 -> respond {
                    color = Color.yellow
                    description = "no songs found for that search"
                    autoDelete = true
                }
                1 -> {
                    player.add(results.first())
                    respond {
                        description = "added ${results.first().identifier} to music player"
                        autoDelete = true
                    }
                    message.delete()
                }
                else -> {
                    if (args<Any>("url") != null) {
                        respond {
                            description = "added ${results.count()} songs to music player"
                            autoDelete = true
                        }
                        player.add(results)
                    } else {
                        message.delete()
                        respondScreen("building song results..", results.toSelectScreen())
                    }
                }
            }
        }

        info {
            description = "plays a song or displays related search results"
            name = "play"
        }

    }

    command("£s {amount}?")
    command("£ skip {amount}?"){
        arguments(strictPositiveInteger("amount"))

        execute {
            val amount = args<Int>("amount") ?: 1
            val musicPlayer = services<MusicPlayer>()

            musicPlayer.skip(amount)
            respond {
                description = "skipped $amount songs${ if(amount > 1) "s" else "" }"
                autoDelete = true
            }
        }

        info {
            description = "skips a number of songs"
            name = "skip"
        }

    }

    command("£s all")
        command("£ skip all"){

        execute {
            val musicPlayer = services<MusicPlayer>()
            musicPlayer.skipAll()
            respond {
                description = "skipped all songs"
                autoDelete = true
            }
        }

        info {
            description = "skips all songs"
            name = "skip"
        }

    }

    command("£st {index}|{name}")
    command("£ skip to {index}|{name}"){
        arguments(strictPositiveInteger("index"), words("name"))

        execute {
            val musicPlayer = services<MusicPlayer>()

            message.delete()

            args<Int>("index")?.let { index ->
                musicPlayer.skip(index)
                respond {
                    description = "skipped ${index - 1} songs"
                    autoDelete = true
                }
                return@execute
            }

            args<String>("name")?.let { name ->
                val index = musicPlayer.scheduler.tracks.indexOfFirst{ it.identifier.toLowerCase().contains(name.toLowerCase()) }
                if(index >= 0) {
                    musicPlayer.skip(index)
                    respond{
                        description = "skipped $index songs"
                        autoDelete = true
                    }
                }else{
                    respondError {
                        description = "no song found with that name"
                        autoDelete = true
                    }
                }
            }
        }

        info {
            description = "skips until a given song or index"
            name = "skip to"
        }
    }
}