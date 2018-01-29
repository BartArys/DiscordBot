package com.numbers.discordbot.service

import com.google.inject.Singleton
import com.numbers.discordbot.module.music.Track
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

@Singleton
class SongSelectService {

    private val cache = mutableMapOf<String,Pair<IMessage,List<Track>>>()

    fun getFor(user: IUser, channel: IChannel) : Pair<IMessage,List<Track>>?{
        return cache["${user.stringID}${channel.stringID}"]
    }

    fun deleteFor(user: IUser, channel: IChannel){
        cache.remove("${user.stringID}${channel.stringID}")
    }

    fun setFor(user: IUser, channel: IChannel, tracks: List<Track>, message: IMessage){
        cache["${user.stringID}${channel.stringID}"] = message to tracks
    }

}