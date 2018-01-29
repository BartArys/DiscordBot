package com.numbers.discordbot.action.music.playlist

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.module.music.MusicManager
import com.numbers.discordbot.module.music.truncatePad
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.service.Permission
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer

class DisplayPlaylistsAction {

    @Permissions(Permission.PLAYLIST)
    @Guards("""
        displays the caller's playlists in this guild
    """,
            Guard("$ playlists")
    )
    fun display(event: MessageReceivedEvent, musicManager: MusicManager){
        launch {
            val formatted = musicManager.playListService.playlistsBy(event.author, event.guild)
                    .joinToString(separator = "\n") { "${it.name!!}: ${it.songs.size} tracks" }.truncatePad(EmbedBuilder.FIELD_CONTENT_LIMIT.toLong())

            RequestBuffer.request { event.message.delete() }
            RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().withDescription(formatted).build()).autoDelete() }
        }
    }

}