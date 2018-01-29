package com.numbers.discordbot.action.music

import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.service.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

class GatherCommand {

    @Permissions(Permission.ADMIN)
    @Guards("gathers all non-bot users in the guild into the voice channel of the caller", Guard("$ gather"), Guard("\$g"))
    fun gather(event: MessageReceivedEvent){
        event.author.getVoiceStateForGuild(event.guild).channel?.let { channel ->
            event.guild.users.filter { !it.isBot && it.getVoiceStateForGuild(event.guild).channel != null }.forEach { it.moveToVoiceChannel(channel) }
        }
    }

}