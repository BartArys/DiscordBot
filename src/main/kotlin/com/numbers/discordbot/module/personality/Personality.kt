package com.numbers.discordbot.module.personality

import com.numbers.discordbot.module.music.Track
import com.numbers.discordbot.network.EightBallResponse
import com.numbers.discordbot.service.PlayList
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel

interface Personality : VoiceChannelPersonality, PlaylistPersonality, GamesPersonality {

    fun exception(throwable: Throwable)

    fun permissionDeniedForUser(permission: String, user: IUser, log: Boolean)

    fun permissionGrantedToUser(permission: String, user: IUser)

    fun bypass(`object`: EmbedObject)

    fun bypass(message: String)

    fun personalityLoaded()

    fun personalityUnloaded()

}

interface VoiceChannelPersonality{

    fun voiceChannelJoin(channel: IVoiceChannel?)

    fun voiceChannelLeave(channel: IVoiceChannel?)

    fun alreadyInVoiceChannel()

    fun failedToJoinVoiceChannel(channel: IVoiceChannel?)

}

interface MusicPlayerPersonality{

    fun noTracksFound(search: String)

    fun trackAdded(track: Track)

    fun tracksAdded(tracks: Iterable<Track>)

    fun skipTrack(amount: Int)

    fun skipAllTracks()

    fun displayQueue(queue: Iterable<Track>)
}

interface PlaylistPersonality : MusicPlayerPersonality{

    fun playlistRemoved(playlist: PlayList)

    fun playlistCreated(playlist: PlayList)

    fun trackAddedToPlaylist(playlist: PlayList, track: Track)

    fun playlistLoading(playlist: PlayList)

    fun playlistLoaded(playlist: PlayList, tracks: Iterable<Track>)

    fun playlistAlreadyExists(playlist: PlayList
    )
}

interface GamesPersonality{

    fun eightBallQuestion(eightBallResponse: EightBallResponse)

}