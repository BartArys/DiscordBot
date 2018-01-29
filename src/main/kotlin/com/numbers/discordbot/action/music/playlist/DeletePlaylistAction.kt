package com.numbers.discordbot.action.music.playlist

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

class DeletePlaylistAction{

    @Permissions(Permission.PLAYLIST)
    @Guards("""
        Deletes a playlist with the given name.

        Raises error when playlist does not exist.
    """,
            Guard("$ delete playlist {playlist name}", Argument(ArgumentType.WORDS, "the name of the playlist")),
            Guard("$ dp {playlist name}", Argument(ArgumentType.WORDS, "the name of the playlist"))
    )
    fun deletePlaylist(event: MessageReceivedEvent, musicManager: MusicManager, args: CommandArguments, personality: Personality){
        launch {
            val result = musicManager.playListService.playlistsBy(event.author, event.guild, args["playlist name"]!!).firstOrNull()

            if(result == null){
                event.message.autoDelete()
                event.channel.sendMessage(personality.noMatches(false).build()).autoDelete()
                return@launch
            }

            event.message.autoDelete(0)
            musicManager.playListService.deletePlaylist(result.id!!)
            event.channel.sendMessage(personality.playlistRemoved(args["playlist name"]!!).build()).autoDelete()
        }
    }
}