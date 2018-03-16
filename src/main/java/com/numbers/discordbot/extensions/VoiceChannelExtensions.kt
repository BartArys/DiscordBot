package com.numbers.discordbot.extensions

import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions

fun IVoiceChannel.canJoin() : Boolean{
    if(isConnected) return true
    if(this.getModifiedPermissions(client.ourUser).contains(Permissions.ADMINISTRATOR)) return true
    if(userLimit > 0){
        if(connectedUsers.count() >= userLimit) return false
    }
    return this.getModifiedPermissions(client.ourUser).contains(Permissions.VOICE_CONNECT)
}

fun IVoiceChannel.isFull() : Boolean = userLimit > 0 && connectedUsers.count() >= userLimit
