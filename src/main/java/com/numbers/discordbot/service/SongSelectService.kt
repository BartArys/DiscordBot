package com.numbers.discordbot.service

import com.google.inject.Singleton
import com.numbers.discordbot.dsl.discord.DiscordMessage
import com.numbers.discordbot.module.music.Track
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser

@Singleton
class SongSelectService {

    private val cache = mutableMapOf<String,Pair<DiscordMessage,List<Track>>>()

    fun getFor(user: IUser, channel: IChannel) : Pair<DiscordMessage,List<Track>>?{
        return cache["${user.stringID}${channel.stringID}"]
    }

    fun deleteFor(user: IUser, channel: IChannel){
        cache.remove("${user.stringID}${channel.stringID}")
    }

    fun setFor(user: IUser, channel: IChannel, tracks: List<Track>, message: DiscordMessage){
        cache["${user.stringID}${channel.stringID}"] = message to tracks
    }
}