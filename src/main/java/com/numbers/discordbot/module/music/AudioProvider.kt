package com.numbers.discordbot.module.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import sx.blah.discord.handle.audio.AudioEncodingType
import sx.blah.discord.handle.audio.IAudioProvider

class AudioProvider(private val audioPlayer: AudioPlayer) : IAudioProvider {
    private var audioFrame: AudioFrame? = null

    override fun isReady(): Boolean {
        audioFrame = audioFrame ?: audioPlayer.provide()

        return audioFrame != null
    }

    override fun provide(): ByteArray? {

        audioFrame = audioFrame ?: audioPlayer.provide()

        return audioFrame?.data.also { audioFrame = null }
    }

    override fun getAudioEncodingType(): AudioEncodingType = AudioEncodingType.OPUS
}
