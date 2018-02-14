package com.numbers.discordbot.commands

import com.numbers.discordbot.dsl.*
import com.numbers.discordbot.extensions.add
import com.numbers.discordbot.extensions.ensurePlayerCreated
import com.numbers.discordbot.extensions.search
import com.numbers.discordbot.extensions.toSongSelectBook
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.service.Permission
import com.numbers.discordbot.service.SongSelectService
import java.awt.Color

@CommandsSupplier
fun musicCommands() = commands {

    command("£p {url}|{search}")
    command("£ play {url}|{search}"){

        arguments(words with key named "search")
        permissions(Permission.MUSIC)

        execute {
            event.ensurePlayerCreated(services(), services())
            val player = services<MusicPlayer>()

            val search = args<String>("url") ?: "ytsearch:${args<String>("search")}"

            val results = player.search(search, author).toList()
            when(results.count()){
                0 -> respond{
                    color = Color.yellow
                    description = "no songs found for that search"
                    autoDelete = true
                }
                1 -> {
                    player.add(results.first())
                    respond{
                        description = "added ${results.first().identifier} to music player"
                        autoDelete = true
                    }
                    message.delete()
                }
                else -> {
                    if(args<Any>("url") != null) {
                        respond{
                            description = "added ${results.count()} songs to music player"
                            autoDelete = true
                        }
                        player.add(results)
                    }else{
                        message.delete()
                        val message = respond { description = "multiple tracks found, select by space separated numbers, 'all' or 'none'" }
                        val songSelectService = services<SongSelectService>()

                        with(results.toSongSelectBook()){
                            onDelete { songSelectService.deleteFor(author, channel) }
                            publish(message)
                        }

                        services<SongSelectService>().setFor(author, channel, results, message)
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
        permissions(Permission.MUSIC)

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
        permissions(Permission.MUSIC)

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
        permissions(Permission.MUSIC)
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

    command("all")
    command("{numbers}"){

        arguments(Sequence.of(positiveInteger("number"), "numbers"))

        execute {
            val service = services<SongSelectService>()
            val result = service.getFor(author, channel) ?: return@execute
            val numbers = args<List<Any>>("numbers")?.map { it as Int } ?: emptyList()
            val player = services<MusicPlayer>()

            message.delete()

            when {
                numbers.isEmpty() -> {
                    result.second.forEach { player.add(it) }
                    result.first.delete()
                    service.deleteFor(author, channel)
                }
                numbers.max()!! > result.second.size -> respondError {
                    description = "number not in list"
                    autoDelete = true
                }
                else -> {
                    numbers.forEach { player.add(result.second[it]) }
                    result.first.delete()
                    service.deleteFor(author, channel)
                }
            }
        }
    }
}