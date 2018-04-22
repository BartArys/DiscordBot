package com.numbers.discordbot.service

import com.numbers.discordbot.module.music.Track
import com.numbers.disko.discord.DiscordMessage
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import javax.inject.Singleton

@Singleton
class SongSelectService {

    private val cache = mutableMapOf<String, Pair<DiscordMessage, List<Track>>>()

    fun getFor(user: IUser, channel: IChannel): Pair<DiscordMessage, List<Track>>? {
        return cache["${user.stringID}${channel.stringID}"]
    }

    fun deleteFor(user: IUser, channel: IChannel) {
        cache.remove("${user.stringID}${channel.stringID}")
    }

    fun setFor(user: IUser, channel: IChannel, tracks: List<Track>, message: DiscordMessage) {
        cache["${user.stringID}${channel.stringID}"] = message to tracks
    }
}