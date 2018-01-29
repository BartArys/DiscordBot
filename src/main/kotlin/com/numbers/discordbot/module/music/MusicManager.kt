package com.numbers.discordbot.module.music

import com.google.inject.Inject
import com.google.inject.Singleton
import com.numbers.discordbot.service.PlaylistService
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import sx.blah.discord.handle.obj.IGuild

interface MusicManager{

    val playListService : PlaylistService

    fun forGuild(guild: IGuild) : MusicPlayer

}

@Singleton
class CachedMusicManager @Inject constructor(override val playListService: PlaylistService): MusicManager{

    private val cache = mutableMapOf<String,MusicPlayer>()

    private val audioPlayerManager = DefaultAudioPlayerManager()

    init {
        audioPlayerManager.enableGcMonitoring()
        AudioSourceManagers.registerRemoteSources(audioPlayerManager)
    }

    override fun forGuild(guild: IGuild): MusicPlayer {
        var player = cache[guild.stringID]

        player?.let { return it }

        player = LavaMusicPlayer(audioPlayerManager)
        guild.audioManager.audioProvider = player
        cache.put(guild.stringID, player)

        return player
    }

}
