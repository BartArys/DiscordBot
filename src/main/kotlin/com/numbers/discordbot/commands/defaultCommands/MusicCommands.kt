package com.numbers.discordbot.commands.defaultCommands

import com.numbers.discordbot.extensions.Empty
import com.numbers.discordbot.extensions.Multiple
import com.numbers.discordbot.extensions.Single
import com.numbers.discordbot.extensions.search
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.module.music.toSelectScreen
import com.numbers.disko.*
import com.numbers.disko.guard.canDeleteMessage
import com.numbers.disko.guard.canSendMessage
import com.numbers.disko.guard.guard
import java.awt.Color

fun musicCommands() = commands {

    command("£p {url}|{search}")
    command("£ play {url}|{search}") {

            arguments(words("search"))

        execute {
            val player = services<MusicPlayer>()

            val search = args<String>("url") ?: "ytsearch:${args<String>("search")}"

            val result = player.search(search, author)
            when (result) {
                is Empty -> {
                    guard( { canSendMessage } ) {
                        respond.autoDelete {
                            color = Color.yellow
                            description = "no songs found for that search"
                        }
                    }

                    guard( { canDeleteMessage } ) { message.deleteLater() }
                }
                is Single -> {
                    player.add(result)
                    respond.autoDelete {
                        description = "added ${result.identifier} to music player"
                    }
                    guard( { canDeleteMessage } ) { message.delete() }
                }
                is Multiple -> {
                    if (args<Any>("url") != null) {
                        guard( { canSendMessage } ){
                            respond.autoDelete {
                                description = "added ${result.tracks.count()} songs to music player"
                            }
                        }
                        result.tracks.forEach(player::add)
                        guard( { canDeleteMessage } ) { message.delete() }
                    } else {
                        message.delete()
                        respond.screen("building song result..", result.tracks.toList().toSelectScreen())
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
    command("£ skip {amount}?") {
        arguments(strictPositiveInteger("amount"))

        execute {
            val amount = args<Int>("amount") ?: 1
            val musicPlayer = services<MusicPlayer>()

            musicPlayer.skip(amount)
            guard({ canSendMessage }) {
                respond {
                    description = "skipped $amount songs${if (amount > 1) "s" else ""}"
                    autoDelete = true
                }
            }
        }

        info {
            description = "skips a number of songs"
            name = "skip"
        }

    }

    command("£s all")
    command("£ skip all") {

        execute {
            val musicPlayer = services<MusicPlayer>()
            musicPlayer.skipAll()
            guard({ canSendMessage }) {
                respond {
                    description = "skipped all songs"
                    autoDelete = true
                }
            }
        }

        info {
            description = "skips all songs"
            name = "skip"
        }

    }

    command("£st {index}|{name}")
    command("£ skip to {index}|{name}") {
        arguments(strictPositiveInteger("index"), words("name"))

        execute {
            val musicPlayer = services<MusicPlayer>()

            guard({ canDeleteMessage }) { message.delete() }

            args<Int>("index")?.let { index ->
                musicPlayer.skip(index)
                guard({ canSendMessage }) {
                    respond {
                        description = "skipped ${index - 1} songs"
                        autoDelete = true
                    }
                }
                return@execute
            }

            args<String>("name")?.let { name ->
                val index = musicPlayer.scheduler.tracks.indexOfFirst { it.identifier.toLowerCase().contains(name.toLowerCase()) }
                if (index >= 0) {
                    musicPlayer.skip(index)
                    guard({ canSendMessage }) {
                        respond.autoDelete {
                            description = "skipped $index songs"
                        }
                    }
                } else {
                    guard({ canSendMessage }) {
                        respond.autoDelete.error {
                            description = "no song found with that name"
                        }
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