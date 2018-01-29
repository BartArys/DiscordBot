package com.numbers.discordbot.action.music

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.info
import com.numbers.discordbot.extensions.then
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.module.music.toEmbeds
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.service.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuilder

class QueueAction {

    @Permissions(Permission.MUSIC)
    @Guards("""
        Displays the current music player's song queue
    """,
            Guard("$ queue|q"),
            Guard("\$q")
    )
    fun queue(event: MessageReceivedEvent, player: MusicPlayer){
        val songs = player.scheduler.tracks

        val requestBuilder = RequestBuilder(event.client)
                .shouldBufferRequests(true).doAction {
            event.message.delete(); true
        }

        if(songs.isEmpty()){
            requestBuilder.then {
                event.channel.sendMessage(EmbedBuilder().info("Queue is currently empty").build()).autoDelete()
            }.execute()
            return
        }

        val builder = EmbedBuilder().info().withTitle("count: ${songs.size}")

        songs.toEmbeds().forEach { builder.appendField(it) }

        requestBuilder.then { event.channel.sendMessage(builder.build()).autoDelete(120) }
        requestBuilder.execute()
    }
}



