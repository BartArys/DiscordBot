package com.numbers.discordbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class SilentAudioResultHandler(private val scheduler : TrackScheduler) : AudioLoadResultHandler{

    override fun loadFailed(exception: FriendlyException?) {}

    override fun trackLoaded(audioTrack: AudioTrack?) {
        audioTrack?.let { scheduler.queue(it) }
    }

    override fun noMatches() {}

    override fun playlistLoaded(audioPlaylist: AudioPlaylist?) {
        audioPlaylist?.let { it.tracks.forEach(scheduler::queue) }
    }

}