package com.numbers.discordbot.module.personality

import com.numbers.discordbot.module.music.Track
import com.numbers.discordbot.network.EightBallResponse
import com.numbers.discordbot.service.PlayList
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset

class SilentPersonality(private val event: MessageReceivedEvent) : Personality{

    override fun exception(throwable: Throwable) {
        val stream = ByteArrayOutputStream(1024 * 50)
        val printStream = PrintStream(stream)
        throwable.printStackTrace(stream = printStream)
        event.client.applicationOwner.orCreatePMChannel.sendMessage(stream.toString(Charset.defaultCharset().displayName()))
    }

    override fun permissionDeniedForUser(permission: String, user: IUser, log: Boolean) {}

    override fun permissionGrantedToUser(permission: String, user: IUser) {}

    override fun bypass(`object`: EmbedObject) {}

    override fun bypass(message: String) {}

    override fun personalityLoaded() {}

    override fun personalityUnloaded() {}

    override fun voiceChannelJoin(channel: IVoiceChannel?) {}

    override fun voiceChannelLeave(channel: IVoiceChannel?) {}

    override fun alreadyInVoiceChannel() {}

    override fun failedToJoinVoiceChannel(channel: IVoiceChannel?) {}

    override fun noTracksFound(search: String) {}

    override fun trackAdded(track: Track) {}

    override fun tracksAdded(tracks: Iterable<Track>) {}

    override fun skipTrack(amount: Int) {}

    override fun skipAllTracks() {}

    override fun displayQueue(queue: Iterable<Track>) {}

    override fun playlistRemoved(playlist: PlayList) {}

    override fun playlistCreated(playlist: PlayList) {}

    override fun trackAddedToPlaylist(playlist: PlayList, track: Track) {}

    override fun playlistLoading(playlist: PlayList) {}

    override fun playlistLoaded(playlist: PlayList, tracks: Iterable<Track>) {}

    override fun playlistAlreadyExists(playlist: PlayList) {}

    override fun eightBallQuestion(eightBallResponse: EightBallResponse) {}


}