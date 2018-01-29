package com.numbers.discordbot.action.music.playlist

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.sendMessageAsync
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

class CreatePlaylistAction{

    @Permissions(Permission.PLAYLIST)
    @Guards("""
        Creates a playlist with the given name.

        Raises error when a playlist already exists with the given name
    """,
            Guard("$ create playlist {playlist name}", Argument(ArgumentType.WORDS, "the name of the playlist")),
            Guard("$ cp {playlist name}", Argument(ArgumentType.WORDS, "the name of the playlist"))
    )
    fun createPlaylist(event: MessageReceivedEvent, args: CommandArguments, musicManager: MusicManager, personality: Personality){
        launch {
            val playlists = musicManager.playListService.playlistsBy(event.author, event.guild, name = args["playlist name"]!!)
            if(playlists.any()){
                event.message.autoDelete()
                event.channel.sendMessageAsync(personality.playlistAlreadyExists(args["playlist name"]!!).build()).autoDelete()
            }else{
                musicManager.playListService.save(emptyList(), args["playlist name"]!!, forGuild = event.guild, forUser = event.author)
                event.message.autoDelete(0)
                event.channel.sendMessage(personality.playListCreated(args["playlist name"]!!, listOf()).build()).autoDelete()
            }
        }
    }

}