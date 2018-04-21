package com.numbers.discordbot.module.music

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import sx.blah.discord.handle.obj.IGuild
import javax.inject.Singleton

interface MusicManager {

    fun playerForGuild(guild: IGuild): MusicPlayer
}

@Singleton
class CachedMusicManager : MusicManager {

    private val cache = mutableMapOf<String, MusicPlayer>()

    private val audioPlayerManager = DefaultAudioPlayerManager()

    init {
        audioPlayerManager.enableGcMonitoring()
        AudioSourceManagers.registerRemoteSources(audioPlayerManager)
    }

    override fun playerForGuild(guild: IGuild): MusicPlayer {
        var player = cache[guild.stringID]

        player?.let { return it }

        player = LavaMusicPlayer(audioPlayerManager, this   )
        guild.audioManager.audioProvider = player
        cache[guild.stringID] = player

        return player
    }

    fun freePlayer(player: MusicPlayer) {
        cache.entries.firstOrNull { it.value == player }?.let {  cache.remove(it.key) }
    }

}
