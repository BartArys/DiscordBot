package com.numbers.discordbot.extensions

import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions

fun IVoiceChannel.canJoin() : Boolean{
    if(isConnected) return true
    if(this.getModifiedPermissions(client.ourUser).contains(Permissions.ADMINISTRATOR)) return true
    if(connectedUsers.count() >= userLimit) return false
    return this.getModifiedPermissions(client.ourUser).contains(Permissions.VOICE_CONNECT)
}
