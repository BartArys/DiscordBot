package com.numbers.discordbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import sx.blah.discord.handle.obj.IGuild

interface MusicManagerCache {

    fun getGuildMusicManager(fromGuild : IGuild) : GuildMusicManager
    val audioPlayerManager : AudioPlayerManager

}