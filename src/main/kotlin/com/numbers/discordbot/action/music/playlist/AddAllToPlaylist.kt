package com.numbers.discordbot.action.music.playlist

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.sendMessageAsync
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.Permission
import com.numbers.discordbot.service.PlaylistService
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.RequestBuffer

class AddAllToPlaylist {

    @Permissions(Permission.PLAYLIST, Permission.MUSIC)
    @Guards("""
        Adds all songs in player to playlist.
    """, Guard("$ add all to playlist {playlist}", Argument(ArgumentType.WORDS, "the playlist name")))
    fun addAll(channel: IChannel, message: IMessage, author: IUser, playlistService: PlaylistService, musicPlayer: MusicPlayer, args: CommandArguments, personality: Personality){
        launch {
            RequestBuffer.request { message.delete() }
            val playlist = playlistService.playlistsBy(author, channel.guild, args["playlist"]!!).firstOrNull()

            if(playlist == null){
                channel.sendMessageAsync(personality.noMatches(args["playlist"]!!).build()).autoDelete()
            }else{
                playlist.songs.addAll(musicPlayer.scheduler.tracks.map { it.identifier })
                channel.sendMessageAsync(personality.songSaved(playlist.name!!).build()).autoDelete()
            }
        }
    }
}