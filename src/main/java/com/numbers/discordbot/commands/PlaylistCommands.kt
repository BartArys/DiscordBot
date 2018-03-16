package com.numbers.discordbot.commands

import com.numbers.discordbot.dsl.CommandsSupplier
import com.numbers.discordbot.dsl.commands
import com.numbers.discordbot.dsl.get
import com.numbers.discordbot.dsl.gui2.Controlled
import com.numbers.discordbot.dsl.gui2.list
import com.numbers.discordbot.dsl.words
import com.numbers.discordbot.extensions.search
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.service.discordservices.Playlist
import com.numbers.discordbot.service.discordservices.PlaylistService
import com.numbers.discordbot.service.discordservices.asSong

@CommandsSupplier
fun playlistCommands() = commands {

    command("£ save song as {playlist}")
    command("£ssa {playlist}"){
        arguments(words("playlist"))

        execute {
            val musicPlayer = services<MusicPlayer>()
            val playlistService = services<PlaylistService>()

            val track = musicPlayer.currentTrack

            if(track == null){
                respondError {
                    description = "no song currently playling"
                    autoDelete = true
                }
                return@execute
            }

            if(playlistService.getPlaylistsForUser(author).filter { it.guild == guild!!.stringID }.map { it.name }.contains(args["playlist"]!!)){
                respondError {
                    description = "playlist with that name already exists"
                    autoDelete = true
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
    command("£ play playlist {playlist}"){
        arguments(words("playlist"))

        execute {
            val playlist = services<PlaylistService>().getPlaylistsByName(args["playlist"]!!).firstOrNull()

            if(playlist == null){
                message.deleteLater()
                respondError {
                    description = "playlist doesn't exist"
                    autoDelete = true
                }
                return@execute
            }

            if(playlist.songs.isEmpty()){
                message.deleteLater()
                respondError {
                    description = "playlist is empty"
                    autoDelete = true
                }
                return@execute
            }

            val player = services<MusicPlayer>()
            message.delete()
            player.skipAll()

            playlist.songs.mapNotNull { player.search(it.url, author).firstOrNull() }
                    .forEach { player.add(it) }
        }

        info {
            description = "plays a user's playlist"
            name = "play playlist"
        }
    }

    command("£p")
    command("£ playlists"){
        execute {
            val service = services<PlaylistService>()
            val playlists = service.getPlaylistsForUser(author)

            println(playlists)

            respond {
                description = playlists.joinToString { it.name }
            }

            respondScreen("looking up playlists") {
                list(playlists) {
                    properties(Controlled)

                    renderIndexed("playlists") { index,  item ->
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
    command("£delete {playlist}"){
        arguments(words("playlist"))

        execute {
            val service = services<PlaylistService>()

            val playlist= service.getPlaylistsForUser(author).filter { it.name == args["playlist"] }.firstOrNull()

            if(playlist == null){
                message.deleteLater()
                respondError {
                    description = "no playlist exists by that name"
                    autoDelete = true
                }
            }else{
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
    command("£ create playlist {playlist}"){
        arguments(words("playlist"))

        execute {
            val service = services<PlaylistService>()

            val playlists = service.getPlaylistsForUser(author).filter { it.name == args["playlist"] && it.guild == guild?.stringID }

            if(playlists.any()){
                message.deleteLater()
                respondError { description = "playlist with that name already exists" }.await().deleteLater()
            }else{
                service.addNewPlaylist(Playlist(name = args["playlist"]!!, songs = listOf(), user = author.stringID, guild = guild!!.stringID))
                message.delete()
                respond { description = "playlist created" }.await().deleteLater()
            }
        }
    }

    command("£atp {playlist}")
    command("£ add to playlist"){

        arguments(words("playlist"))

        execute {
            val track = services<MusicPlayer>().currentTrack
            val service = services<PlaylistService>()

            val playlist = service.getPlaylistsForUser(author).firstOrNull { it.name == args["playlist"] }

            when {
                track == null -> {
                    message.deleteLater()
                    respondError {
                        description = "no track is playing"
                        autoDelete = true
                    }
                }
                playlist == null -> {
                    message.deleteLater()
                    respondError {
                        description = "playlist does not exist"
                        autoDelete = true
                    }
                }
                else -> {
                    service.addSongToPlaylist(playlist, track.asSong)
                    message.delete()
                    respond {
                        "track added to ${playlist.name}" }
                }
            }
        }

        info {
            description = "adds currently playing song to playlist"
            name = "add to playlist"
        }

    }

    command("£aatp {playlist}")
    command("£ add all to playlist {playlist}"){

        arguments(words("playlist"))

        execute {
            message.delete()

            val service = services<PlaylistService>()
            val playlist = service.getPlaylistsForUser(author).firstOrNull { it.name == args["playlist"] }

            if(playlist == null){
                respondError {
                    description = "no playlists found for that parameter"
                    autoDelete = true
                }
            }else{
                val musicPlayer = services<MusicPlayer>()
                musicPlayer.scheduler.tracks.forEach {
                    service.addSongToPlaylist(playlist, it.asSong )
                }
                respond {
                    description = "added songs to ${playlist.name}"
                    autoDelete = true
                }
            }
        }

        info {
            description = "adds all queued songs to playlist"
            name = "add all to playlist"
        }

    }

}