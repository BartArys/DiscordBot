package com.numbers.discordbot.action.music.playlist

import com.google.inject.Singleton
import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.search
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

@Singleton
class PlayPlaylistAction {

    @Permissions(Permission.PLAYLIST)
    @Guards("""
        Clears the current music player (if present) and loads all tracks from the playlist.

        Raises error when playlist does not exist
    """,
            Guard("$ playlist play {playlist}", Argument(ArgumentType.WORDS, "the name of the playlist")),
            Guard("$ pp {playlist}", Argument(ArgumentType.WORDS, "the name of the playlist"))
    )
    fun play(event: MessageReceivedEvent, args: CommandArguments, personality: Personality, musicManager: MusicManager, musicPlayer: MusicPlayer){
        launch {
            val playlist = musicManager.playListService.playlistsBy(user = event.author, guild = event.guild, name = args["playlist"]!!).firstOrNull()
            event.message.autoDelete()

            playlist.takeIf { it == null || it.songs.isEmpty() }?.let {
                event.channel.sendMessageAsync(personality.noMatches(false).build()).autoDelete()
                return@launch
            }

            musicPlayer.skipAll()
            event.message.autoDelete()
            event.channel.sendMessageAsync(personality.playlistLoading(playlist!!.songs).build()).autoDelete()

            playlist.songs.forEach {

                val song = try {
                    musicPlayer.search(it, event.author).firstOrNull()
                } catch (ex: Exception){
                    event.channel.sendMessage(personality.songLoadFailed(ex).build()).autoDelete()
                    return@forEach
                }

                if(song == null){
                    event.channel.sendMessage(personality.noMatches(false).build()).autoDelete()
                }else{
                    musicPlayer.add(song)
                }
            }
        }
    }

}
