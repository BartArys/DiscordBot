package com.numbers.discordbot.commands.defaultCommands

import com.numbers.discordbot.dsl.*
import com.numbers.discordbot.dsl.guard.canSendMessage
import com.numbers.discordbot.dsl.guard.guard
import com.numbers.discordbot.dsl.gui2.Controlled
import com.numbers.discordbot.dsl.gui2.list
import com.numbers.discordbot.extensions.Multiple
import com.numbers.discordbot.extensions.Single
import com.numbers.discordbot.extensions.search
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.service.discordservices.Playlist
import com.numbers.discordbot.service.discordservices.PlaylistService
import com.numbers.discordbot.service.discordservices.asSong

@CommandsSupplier
fun playlistCommands() = commands {

    command("£ save song as {playlist}")
    command("£ssa {playlist}") {
        arguments(words("playlist"))

        execute {
            val musicPlayer = services<MusicPlayer>()
            val playlistService = services<PlaylistService>()

            val track = musicPlayer.currentTrack

            if (track == null) {
                guard({ canSendMessage }) {
                    respond.autoDelete.error {
                        description = "no song currently playing"
                    }
                }
                return@execute
            }

            if (playlistService.getPlaylistsForUser(author).filter { it.guild == guild!!.stringID }.map { it.name }.contains(args["playlist"]!!)) {
                guard({ canSendMessage }) {
                    respond.error {
                        description = "playlist with that name already exists"
                    }
                }
                return@execute
            }

            playlistService.addNewPlaylist(Playlist(name = args["playlist"]!!, guild = guild!!.stringID, user = author.stringID, songs = listOf(track.asSong)))
            respond {
                description = "playlist created"
                autoDelete = true
            }
        }

        info {
            description = "saves the currently playling song to a new playlist"
            name = "save song"
        }
    }

    command("£pp {playlist}")
    command("£ play playlist {playlist}") {
        arguments(words("playlist"))

        execute {
            val playlist = services<PlaylistService>().getPlaylistsByName(args["playlist"]!!).firstOrNull()

            if (playlist == null) {
                message.deleteLater()
                respond.autoDelete.error {
                    description = "playlist doesn't exist"
                }
                return@execute
            }

            if (playlist.songs.isEmpty()) {
                message.deleteLater()
                respond.error {
                    description = "playlist is empty"
                }
                return@execute
            }

            val player = services<MusicPlayer>()
            message.delete()
            player.skipAll()

            playlist.songs.mapNotNull {
                val result = player.search(it.url, author)
                when(result) {
                    is Single -> result
                    is Multiple -> result.tracks.first()
                    else -> {
                        respond.error.autoDelete { description = "can't load track for ${it.name}" }
                        null
                    }
                }
            }.forEach { player.add(it) }
        }

        info {
            description = "plays a user's playlist"
            name = "play playlist"
        }
    }

    command("£p")
    command("£ playlists") {
        execute {
            val service = services<PlaylistService>()
            val playlists = service.getPlaylistsForUser(author)

            println(playlists)

            respond {
                description = playlists.joinToString { it.name }
            }

            respond.screen("looking up playlists") {
                list(playlists) {
                    properties(Controlled)

                    renderIndexed("playlists") { index, item ->
                        "$index: ${item.name} [${item.songs.size}]"
                    }
                }
            }
        }

        info {
            description = "displays all user's playlists in this guild"
            name = "display playlists"
        }
    }

    command("£dp {playlist}")
    command("£delete {playlist}") {
        arguments(words("playlist"))

        execute {
            val service = services<PlaylistService>()

            val playlist = service.getPlaylistsForUser(author).filter { it.name == args["playlist"] }.firstOrNull()

            if (playlist == null) {
                message.deleteLater()
                respond.error.autoDelete {
                    description = "no playlist exists by that name"
                }
            } else {
                service.deletePlaylist(playlist)
                message.delete()
                respond {
                    description = "playlist removed"
                    autoDelete = true
                }
            }
        }

        info {
            description = "deletes an existing playlist"
            name = "delete playlist"
        }
    }

    command("£cp {playlist}")
    command("£ create playlist {playlist}") {
        arguments(words("playlist"))

        execute {
            val service = services<PlaylistService>()

            val playlists = service.getPlaylistsForUser(author).filter { it.name == args["playlist"] && it.guild == guild?.stringID }

            if (playlists.any()) {
                message.deleteLater()
                respond.error { description = "playlist with that name already exists" }.await().deleteLater()
            } else {
                service.addNewPlaylist(Playlist(name = args["playlist"]!!, songs = listOf(), user = author.stringID, guild = guild!!.stringID))
                message.delete()
                respond { description = "playlist created" }.await().deleteLater()
            }
        }

        info {
            name = "create playlist"
            description = "creates a new, empty playlist with the given name"
        }
    }

    command("£atp {playlist}")
    command("£ add to playlist") {

        arguments(words("playlist"))

        execute {
            val track = services<MusicPlayer>().currentTrack
            val service = services<PlaylistService>()

            val playlist = service.getPlaylistsForUser(author).firstOrNull { it.name == args["playlist"] }

            when {
                track == null -> {
                    message.deleteLater()
                    respond.autoDelete.error {
                        description = "no track is playing"
                    }
                }
                playlist == null -> {
                    message.deleteLater()
                    respond.error {
                        description = "playlist does not exist"
                    }
                }
                else -> {
                    service.addSongToPlaylist(playlist, track.asSong)
                    message.delete()
                    respond {
                        "track added to ${playlist.name}"
                    }
                }
            }
        }

        info {
            description = "adds currently playing song to playlist"
            name = "add to playlist"
        }

    }

    command("£aatp {playlist}")
    command("£ add all to playlist {playlist}") {

        arguments(words("playlist"))

        execute {
            message.delete()

            val service = services<PlaylistService>()
            val playlist = service.getPlaylistsForUser(author).firstOrNull { it.name == args["playlist"] }

            if (playlist == null) {
                respond.error.autoDelete {
                    description = "no playlists found for that parameter"
                }
            } else {
                val musicPlayer = services<MusicPlayer>()
                musicPlayer.scheduler.tracks.forEach {
                    service.addSongToPlaylist(playlist, it.asSong)
                }
                respond.autoDelete {
                    description = "added songs to ${playlist.name}"
                }
            }
        }

        info {
            description = "adds all queued songs to playlist"
            name = "add all to playlist"
        }

    }

}