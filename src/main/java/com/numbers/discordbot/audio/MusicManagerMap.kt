package com.numbers.discordbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import sx.blah.discord.handle.obj.IGuild

class MusicManagerMap : MusicManagerCache {
    private val cache : MutableMap<String, GuildMusicManager> = HashMap()
    override val audioPlayerManager: AudioPlayerManager

    init {
        audioPlayerManager = DefaultAudioPlayerManager()
        audioPlayerManager.registerSourceManager(YoutubeAudioSourceManager(true))
        AudioSourceManagers.registerRemoteSources(audioPlayerManager)
    }

    override fun getGuildMusicManager(fromGuild: IGuild): GuildMusicManager {
        var gmm : GuildMusicManager? = cache[fromGuild.stringID]

        if(gmm == null){
            gmm = GuildMusicManager(audioPlayerManager)
            cache.put(fromGuild.stringID, gmm)
            fromGuild.audioManager.audioProvider = gmm.audioProvider
        }

        return gmm
    }

}