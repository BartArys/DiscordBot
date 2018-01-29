package com.numbers.discordbot.action.music

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.DisplayMessageService
import com.numbers.discordbot.service.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RequestBuffer

class LeaveAction{

    @Permissions(Permission.MUSIC)
    @Guards("""
        leaves the currently connected voice channel if any
    """,
            Guard("$ leave|l"),
            Guard("\$l"))
    fun handle(event: MessageReceivedEvent, personality: Personality, service: DisplayMessageService) {
        event.guild.connectedVoiceChannel?.let {
            it.leave()
            event.message.delete()
            service.messages.removeIf {
                if(it.message.guild == event.message.guild) {
                    RequestBuffer.request { it.close() }
                    true
                }else{
                    false
                }
            }
            event.channel.sendMessage(personality.voiceChannelLeave(it).build()).autoDelete()
        }
    }

}