package com.numbers.discordbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.stream.Stream

class TrackScheduler(private val player: AudioPlayer) : AudioEventAdapter() {

    private val queue : Queue<AudioTrack> = LinkedBlockingQueue<AudioTrack>()

    fun getQueueStream() : Stream<AudioTrack> { return queue.stream() }

    val size : Int get() = queue.size

    val isEmpty : Boolean get() = queue.isEmpty()

    fun clear() = queue::clear

    fun remove(amount : Long ) = (0..amount).forEach { queue.poll() }

    fun queue(track: AudioTrack) {
        if(!player.startTrack(track, true)){
            queue.offer(track)
        }
    }

    fun nextTrack() { player.startTrack(queue.poll(), false) }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if(endReason.mayStartNext){
            nextTrack()
        }
    }

}
