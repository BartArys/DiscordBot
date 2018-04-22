package com.numbers.discordbot.module.music

import com.numbers.discordbot.extensions.Single
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import sx.blah.discord.handle.audio.AudioEncodingType
import sx.blah.discord.handle.obj.IUser
import java.time.Duration

class LavaMusicPlayer(private val manager: AudioPlayerManager, private val musicManager: CachedMusicManager) : MusicPlayer, AudioEventAdapter() {

    private val audioPlayer: AudioPlayer = manager.createPlayer()

    private var _scheduler: Scheduler = PlaylistScheduler()

    override val currentTrackProperty = scheduler.currentTrackProperty

    override val pausedProperty: SimpleBooleanProperty = SimpleBooleanProperty(audioPlayer.isPaused)

    override val volumeProperty: SimpleIntegerProperty = SimpleIntegerProperty(audioPlayer.volume)

    private val audioProvider: AudioProvider = AudioProvider(audioPlayer)

    override val eventListeners: MutableList<MusicEventListener> = mutableListOf()

    override var volume: Int
        get() {
            return audioPlayer.volume
        }
        set(value) {
            audioPlayer.volume = value
            volumeProperty.set(audioPlayer.volume)
        }

    override var isPaused: Boolean
        get() {
            return audioPlayer.isPaused
        }
        set(value) {
            audioPlayer.isPaused = value
            pausedProperty.set(audioPlayer.isPaused)
        }


    override var scheduler: Scheduler
        get() {
            return _scheduler
        }
        set(value) {
            value.tracks.clear()
            value.tracks.addAll(_scheduler.remaining)
        }

    init {
        audioPlayer.addListener(this)
    }

    override fun skip(amount: Int) {
        audioPlayer.stopTrack()
        (0 until amount).forEach { scheduler.skip() }
        _scheduler.current?.let { audioPlayer.startTrack((it as LavaTrack).track, true) }
    }

    override fun skipAll() {
        audioPlayer.stopTrack()
        skip(_scheduler.tracks.count())
    }

    override fun add(track: Track) {
        super.add(track)
        audioPlayer.startTrack(((track as Single).track as LavaTrack).track, true)
    }

    override fun add(index: Int, track: Track) {
        super.add(index, track)
        if (audioPlayer.playingTrack == null && index == 0) audioPlayer.startTrack((track as LavaTrack).track, false)
    }

    override fun addToFront(track: Track) {
        super.addToFront(track)
        if (audioPlayer.playingTrack == null) audioPlayer.startTrack((track as LavaTrack).track, false)
    }

    override fun search(search: String, user: IUser, callback: SearchResultHandler) {
        manager.loadItem(search, object : AudioLoadResultHandler {
            override fun loadFailed(exception: FriendlyException) {
                callback.onFailed(search, exception)
            }

            override fun trackLoaded(track: AudioTrack) {
                callback.onFindOne(search, LavaTrack(user, track))
            }

            override fun noMatches() {
                callback.onFindNone(search)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val lavaTracks = playlist.tracks.map { LavaTrack(user, it) }
                callback.onFindMultiple(search, lavaTracks)
            }

        })
    }

    override fun onPlayerPause(player: AudioPlayer) {
        eventListeners.forEach { it.onTrackPause(this) }
    }

    override fun onPlayerResume(player: AudioPlayer) {
        eventListeners.forEach { it.onTrackResume(this) }
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        val reason = when (endReason) {
            AudioTrackEndReason.CLEANUP -> TrackEndReason.Cleaned
            AudioTrackEndReason.FINISHED -> TrackEndReason.Finished
            AudioTrackEndReason.LOAD_FAILED -> TrackEndReason.LoadFailed
            AudioTrackEndReason.STOPPED -> TrackEndReason.Stopped
            AudioTrackEndReason.REPLACED -> TrackEndReason.Replaced
        }

        val stoppedTrack = currentTrack!!

        if (reason.mayStartNext && _scheduler.next() != null) {
            player.startTrack((currentTrack as LavaTrack).track, true)
        }

        eventListeners.forEach { it.onTrackEnd(this, stoppedTrack, reason = reason) }

    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        eventListeners.forEach { it.onTrackException(this, currentTrack!!, exception) }
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        eventListeners.forEach { it.onTrackStart(this, currentTrack!!) }
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        eventListeners.forEach { it.onTrackStuck(this, currentTrack!!, Duration.ofMillis(thresholdMs)) }
    }

    override fun isReady(): Boolean {
        return audioProvider.isReady
    }

    override fun provide(): ByteArray? {
        return audioProvider.provide()
    }

    override fun getAudioEncodingType(): AudioEncodingType {
        return audioProvider.audioEncodingType
    }

    override fun getChannels(): Int {
        return audioProvider.channels
    }

    override fun destroy() {
        musicManager.freePlayer(this)
        audioPlayer.destroy()
        scheduler.tracks.clear()
    }

}

class LavaTrack(override val requestedBy: IUser, val track: AudioTrack) : Track {

    override val duration: Duration
        get() {
            return Duration.ofMillis(track.duration)
        }
    override val position: Duration
        get() {
            return Duration.ofMillis(track.position)
        }
    override val identifier: String
        get() {
            return track.info.title
        }
    override val author: String
        get() {
            return track.info.author
        }
    override val isStream: Boolean
        get() {
            return track.info.isStream
        }
    override val url: String
        get() {
            return track.info.uri
        }

    override fun seak(duration: Duration) {
        track.position = duration.toMillis()
    }
}
