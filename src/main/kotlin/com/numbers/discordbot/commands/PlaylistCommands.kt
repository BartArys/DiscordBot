package com.numbers.discordbot.commands

import com.numbers.discordbot.dsl.*
import com.numbers.discordbot.extensions.search
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.service.Permission
import com.numbers.discordbot.service.PlaylistService
import sx.blah.discord.util.EmbedBuilder

@CommandsSupplier
fun playlistCommands() = commands {

    command("£ save song as {playlist}")
    command("£ssa {playlist}"){
        arguments(words("playlist"))
        permissions(Permission.PLAYLIST)

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

            if(playlistService.playlistsBy(author, guild, args["playlist"]).any()){
                respondError {
                    description = "playlist with that name already exists"
                    autoDelete = true
                }
                return@execute
            }

            playlistService.save(listOf(track), args["playlist"]!!, guild, author)
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
        permissions(Permission.PLAYLIST)
        arguments(words("playlist"))

        execute {
            val playlist = services<PlaylistService>().playlistsBy(author, guild, args["playlist"]).firstOrNull()

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

            playlist.songs.mapNotNull { player.search(it, author).firstOrNull() }
                    .forEach { player.add(it) }
        }

        info {
            description = "plays a user's playlist"
            name = "play playlist"
        }
    }

    command("£p")
    command("£ playlists"){
        permissions(Permission.PLAYLIST)

        execute {
            val message = respond { description = "looking up playlists..." }

            val service = services<PlaylistService>()
            val playlists = service.playlistsBy(author, guild)

            val book = playlists.mapIndexed { index, it ->  "$index: ${it.name}: ${it.songs.size} tracks\n" }
                    .windowed(EmbedBuilder.DESCRIPTION_CONTENT_LIMIT)
                    .bind { description = item.joinToString() }

            message.publish(book)
        }

        info {
            description = "displays all user's playlists in this guild"
            name = "display playlists"
        }
    }

    command("£dp {playlist}")
    command("£delete {playlist}"){
        arguments(words("playlist"))
        permissions(Permission.PLAYLIST)

        execute {
            val service = services<PlaylistService>()

            val playlist = service.playlistsBy(author, guild, args["playlist"]).firstOrNull()

            if(playlist == null){
                message.deleteLater()
                respondError { description = "no playlist exists by that name" }.deleteLater()
            }else{
                service.deletePlaylist(playlist.id!!)
                message.delete()
                respond { description = "playlist removed" }.deleteLater()
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
        permissions(Permission.PLAYLIST)

        execute {
            val service = services<PlaylistService>()

            val playlists = service.playlistsBy(author, guild, args["playlist"])

            if(playlists.any()){
                message.deleteLater()
                respondError { description = "playlist with that name already exists" }.deleteLater()
            }else{
                service.save(emptyList(), args["playlist"]!!, guild, author)
                message.delete()
                respond { description = "playlist created" }.deleteLater()
            }
        }
    }

    command("£atp {playlist}")
    command("£ add to playlist"){

        arguments(words("playlist"))
        permissions(Permission.PLAYLIST)

        execute {
            val track = services<MusicPlayer>().currentTrack
            val service = services<PlaylistService>()

            val playlist = service.playlistsBy(author, guild, args["playlist"]).firstOrNull()

            when {
                track == null -> {
                    message.deleteLater()
                    respondError { description = "no track is playing" }.deleteLater()
                }
                playlist == null -> {
                    message.deleteLater()
                    respondError { description = "playlist does not exist" }.deleteLater()
                }
                else -> {
                    playlist.songs.add(track.url)
                    service.save(playlist)
                    message.delete()
                    respond { "track added to ${playlist.name}" }
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
        permissions(Permission.PLAYLIST)

        execute {
            message.delete()

            val service = services<PlaylistService>()
            val playlist = service.playlistsBy(author, guild, args["playlist"]).firstOrNull()

            if(playlist == null){
                respondError { description = "no playlists found for that parameter" }.deleteLater()
            }else{
                val musicPlayer = services<MusicPlayer>()
                playlist.songs.addAll(musicPlayer.scheduler.tracks.map { it.identifier })
                respond { description = "added songs to ${playlist.name}" }.deleteLater()
            }
        }

        info {
            description = "adds all queued songs to playlist"
            name = "add all to playlist"
        }

    }

}