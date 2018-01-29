package com.numbers.discordbot.module.personality

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.error
import com.numbers.discordbot.extensions.info
import com.numbers.discordbot.extensions.reportToAppOwner
import com.numbers.discordbot.module.music.Track
import com.numbers.discordbot.network.EightBallResponse
import com.numbers.discordbot.service.PlayList
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset

class DefaultPersonality(private val event: MessageReceivedEvent) : Personality{

    override fun exception(throwable: Throwable) {
        val stream = ByteArrayOutputStream(1024 * 50)
        val printStream = PrintStream(stream)
        throwable.printStackTrace(stream = printStream)
        RequestBuffer.request { event.client.applicationOwner.orCreatePMChannel.sendMessage(stream.toString(Charset.defaultCharset().displayName())) }
    }

    override fun permissionDeniedForUser(permission: String, user: IUser, log: Boolean) {
        if(log) event.reportToAppOwner()

        RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().info("${user.mention(true)} is not in the $permission file, access is denied${ if(log) ". This incident will be reported" else "" }").build()) }
    }

    override fun permissionGrantedToUser(permission: String, user: IUser) {
        RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().info().withDesc("${user.mention(true)} has been added to the $permission file").build()).autoDelete() }
    }

    override fun bypass(`object`: EmbedObject) {
        RequestBuffer.request { event.channel.sendMessage(`object`) }
    }

    override fun bypass(message: String) {
        RequestBuffer.request { event.channel.sendMessage(message) }
    }

    override fun personalityLoaded() {
        RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().info().withDesc("default personality loaded").build()).autoDelete() }
    }

    override fun personalityUnloaded() { /*none*/ }

    override fun voiceChannelJoin(channel: IVoiceChannel?) {
        val channelName = channel?.name ?: "channel"
        RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().info().withDesc("joined $channelName").build()).autoDelete() }
    }

    override fun voiceChannelLeave(channel: IVoiceChannel?) {
        val channelName = channel?.name ?: "channel"
        RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().info().withDesc("joined $channelName").build()).autoDelete() }
    }

    override fun alreadyInVoiceChannel() {
        val channelName = event.guild.connectedVoiceChannel?.name ?: "voiceChannel"
        RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().error().withDesc("already in $channelName").build()).autoDelete() }
    }

    override fun failedToJoinVoiceChannel(channel: IVoiceChannel?) {
        if(channel == null){
            RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().error().withDesc("failed to join channel").build()).autoDelete() }
            return
        }

        val permissions = channel.getModifiedPermissions(event.client.ourUser)
        val reason = if(!permissions.contains(Permissions.VOICE_CONNECT)){
            "lacking voice connect permission for channel"
        }else if(channel.connectedUsers.count() >= channel.userLimit){
            "channel is fill"
        }else{
            "unknown"
        }

        RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().error().withDesc("cannot join. \nReason: $reason").build()).autoDelete() }
    }

    override fun noTracksFound(search: String) {
        RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().error().withDesc("no tracks found for s$search").build()).autoDelete() }
    }

    override fun trackAdded(track: Track) {
        RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().error().withDesc("${track.identifier} added to player").build()).autoDelete() }
    }

    override fun tracksAdded(tracks: Iterable<Track>) {
        RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().error().withDesc("${tracks.count()} tracks added to player").build()).autoDelete() }
    }

    override fun skipTrack(amount: Int) {
        RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().error().withDesc("$amount tracks skipped").build()).autoDelete() }
    }

    override fun skipAllTracks() {
        RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().error().withDesc("all tracks skipped").build()).autoDelete() }
    }

    override fun displayQueue(queue: Iterable<Track>) {

    }

    override fun playlistRemoved(playlist: PlayList) {

    }

    override fun playlistCreated(playlist: PlayList) {

    }

    override fun trackAddedToPlaylist(playlist: PlayList, track: Track) {

    }

    override fun playlistLoading(playlist: PlayList) {

    }

    override fun playlistLoaded(playlist: PlayList, tracks: Iterable<Track>) {

    }

    override fun playlistAlreadyExists(playlist: PlayList) {

    }

    override fun eightBallQuestion(eightBallResponse: EightBallResponse) {

    }

}