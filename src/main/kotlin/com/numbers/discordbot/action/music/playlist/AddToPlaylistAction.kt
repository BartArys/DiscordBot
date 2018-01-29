package com.numbers.discordbot.action.music.playlist

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.sendMessageAsync
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.module.music.MusicManager
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.Permission
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

class AddToPlaylistAction{

    @Permissions(Permission.PLAYLIST)
    @Guards("""
        adds the current song to the given playlist.

        Raises error when there is no current song or when the playlist doesn't exist.
    """,
            Guard("$ add to playlist {playlist name}", Argument(ArgumentType.WORDS, "the name of the playlist")),
            Guard("$ atp {playlist name}", Argument(ArgumentType.WORDS, "the name of the playlist"))
    )
    fun addToPlaylist(event: MessageReceivedEvent, args: CommandArguments, player: MusicPlayer, musicManager: MusicManager, personality: Personality){
        launch {
            val track = player.currentTrack

            val playlist =  musicManager.playListService.playlistsBy(event.author, event.guild, args["playlist name"]!!).firstOrNull()

            if(playlist == null){
                event.message.autoDelete()
                event.channel.sendMessageAsync(personality.noMatches(false, args["playlist name"]!!).build()).autoDelete()
                return@launch
            }

            if(track == null){
                event.message.autoDelete()
                event.channel.sendMessageAsync(personality.noSongPlaying().build()).autoDelete()
                return@launch
            }


            playlist.songs.add(track.url)
            event.message.autoDelete(0)
            event.channel.sendMessageAsync(personality.songSaved(playlist = playlist.name!!).build()).autoDelete()
        }
    }
}