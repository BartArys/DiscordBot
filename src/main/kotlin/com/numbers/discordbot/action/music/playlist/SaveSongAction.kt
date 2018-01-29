package com.numbers.discordbot.action.music.playlist

import com.google.inject.Singleton
import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.module.music.MusicManager
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.Permission
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RequestBuffer

@Singleton
class SaveSongAction {

    @Permissions(Permission.PLAYLIST, Permission.MUSIC)
    @Guards("""
                Saves current song to a new playlist.

                Raises error when no song is playing or the playlist already exists
            """,
            Guard("$ save song as {name}", Argument(ArgumentType.WORDS, "the name of the playlist")),
            Guard("$ ssa {name}", Argument(ArgumentType.WORDS, "the name of the playlist"))
    )
    fun save(event: MessageReceivedEvent, args: CommandArguments, personality: Personality, musicManager: MusicManager){
        val track = musicManager.forGuild(event.guild).currentTrack
        RequestBuffer.request { event.message.delete() }

        if(track == null){
            RequestBuffer.request { event.channel.sendMessage(personality.noSongPlaying().build()).autoDelete() }
            return
        }

        launch {
            if(musicManager.playListService.playlistsBy(event.author, event.guild, args["name"]!!).any()){
                RequestBuffer.request {
                    event.channel.sendMessage(personality.playlistAlreadyExists(args["name"]!!).build()).autoDelete()
                }
            }else{
                musicManager.playListService.save(forUser = event.author, forGuild = event.guild, tracks = listOf(track), name = args["name"]!!)
                event.channel.sendMessage(personality.playListCreated(args["name"]!!).build()).autoDelete()
            }
        }
    }

}