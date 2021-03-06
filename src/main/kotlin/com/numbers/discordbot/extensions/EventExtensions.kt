package com.numbers.discordbot.extensions

import com.numbers.discordbot.message.MusicPlayerMessage
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.service.DisplayMessageService
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.RequestBuilder

fun MessageEvent.ensureChannelJoined(): IVoiceChannel? {
    client.ourUser.getVoiceStateForGuild(guild).channel?.let { return it }
    author.getVoiceStateForGuild(guild).channel?.let { it.join(); return it }
    guild.voiceChannels.firstOrNull { it.getModifiedPermissions(client.ourUser).contains(Permissions.VOICE_CONNECT) }
            ?.let { it.join(); return it }
    return null
}

fun MessageEvent.ensurePlayerCreated(service: DisplayMessageService, player: MusicPlayer) {
    ensureChannelJoined()?.let {
        if (service.messages.any { it.message.guild.stringID == guild.stringID }) return

        var playerMessage: MusicPlayerMessage? = null
        RequestBuilder(client).shouldBufferRequests(true).doAction {
            val sendMessage = channel.sendMessage("Creating player...")
            playerMessage = MusicPlayerMessage(player, sendMessage, { service.messages.removeIf { it.message.stringID == sendMessage.stringID } })
            true
        }.then {
            playerMessage!!.init()
            service.messages += playerMessage!!
        }.build()
    }
}

