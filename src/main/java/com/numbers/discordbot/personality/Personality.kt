package com.numbers.discordbot.personality

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.error
import com.numbers.discordbot.extensions.info
import com.numbers.discordbot.extensions.success
import com.numbers.discordbot.module.astolfo.random
import com.numbers.discordbot.module.music.Track
import com.numbers.discordbot.network.EightBallResponse
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer

interface Personality {

    fun error(t : Throwable) : EmbedBuilder {
        return EmbedBuilder().error(t.localizedMessage, t)
    }

    fun voiceChannelJoin(voiceChannel: IVoiceChannel): EmbedBuilder {
        return EmbedBuilder().info("joined ${voiceChannel.name}")
    }

    fun voiceChannelLeave(voiceChannel: IVoiceChannel): EmbedBuilder {
        return EmbedBuilder().info("left ${voiceChannel.name}")
    }

    fun voiceChannelLeaveAlone(voiceChannel: IVoiceChannel): EmbedBuilder {
        return EmbedBuilder().info("left ${voiceChannel.name}: no users")
    }

    fun alreadyInChannel(voiceChannel: IVoiceChannel): EmbedBuilder {
        return EmbedBuilder().error("already in channel")
    }

    fun failedToJoinChannel(): EmbedBuilder {
        return EmbedBuilder().error("couldn't join any channel, make sure you gave me the right permissionsl")
    }

    fun noMatches(retrying: Boolean, search: String? = null): EmbedBuilder {
        return if (retrying) {
            EmbedBuilder().error("no results found ${ search?.let { "for $it" } ?: "with that keyword" }, loosening search restrictions")
        } else {
            EmbedBuilder().error("no results found ${ search?.let { "for $it" } ?: "with that keyword" }")
        }
    }

    fun songFound(track: Track): EmbedBuilder {
        return EmbedBuilder().info("added track: ${track.identifier}\"")
    }

    fun songLoadFailed(exception: Exception): EmbedBuilder {
        return EmbedBuilder().error("[${exception.message}] failed to add track", exception)
    }

    fun skipAll(): EmbedBuilder {
        return EmbedBuilder().info("skipped all songs")
    }

    fun skipSongs(amount: Int): EmbedBuilder {
        return EmbedBuilder().info("skipped $amount songs")
    }

    fun numberRequired(): EmbedBuilder {
        return EmbedBuilder().error("number required")
    }

    fun playlistFound(playlist: Iterable<Track>): EmbedBuilder {
        return EmbedBuilder().info("added ${playlist.count()} tracks")
    }

    fun playlistLoading(playlist: Iterable<String>) : EmbedBuilder{
        return EmbedBuilder().info("loading playlist with ${playlist.count()} songs")
    }

    fun playlistRemoved(withName: String) : EmbedBuilder{
        return EmbedBuilder().success("deleted playlist $withName")
    }

    fun selectSong(playlist: Iterable<Track>) : EmbedBuilder{
       return EmbedBuilder().info("multiple tracks found, select by space separated numbers, 'all' or 'none'")
    }

    fun emptyQueue(): EmbedBuilder {
        return EmbedBuilder().error("queue is empty")
    }

    fun switchedTo(): EmbedBuilder {
        return EmbedBuilder().info("enabled default no-op personality")
    }

    fun eightBall(eightBallResponse: EightBallResponse): EmbedBuilder {
        return if (eightBallResponse.type == "Affirmative") {
            EmbedBuilder().success(eightBallResponse.answer)
        } else {
            EmbedBuilder().error(eightBallResponse.answer)
        }
    }

    fun newPrefix(user: IUser, prefix: String): EmbedBuilder {
        return EmbedBuilder().info("prefix set to $prefix for user ${user.mention()}")
    }

    fun clap(): EmbedBuilder {
        return EmbedBuilder().info(":clap: :clap: :clap:")
    }

    fun lackPermission(): EmbedBuilder {
        return EmbedBuilder().info("user is not in the admin document. This incident will be reported")
    }

    fun clapDenied(): EmbedBuilder {
        return EmbedBuilder().error("user is not in the clapPermission document. This incident will be reported")
    }

    fun clapAllowed(user: IUser): EmbedBuilder {
        return EmbedBuilder().info("user ${user.mention()} can now receive applause")
    }

    fun noSongPlaying() : EmbedBuilder{
        return EmbedBuilder().error("no song playing")
    }

    fun songSaved(playlist : String) : EmbedBuilder{
        return EmbedBuilder().info("song saved to $playlist")
    }

    fun playListCreated(name: String, tracks: Iterable<Track> = listOf()) : EmbedBuilder{
        return EmbedBuilder().success("playlist $name created with ${tracks.count()}")
    }

    fun playlistAlreadyExists(name: String) : EmbedBuilder{
        return EmbedBuilder().error("playlist $name already exits")
    }


    fun <T> trigger(trigger: T) {
        if (trigger is UserVoiceChannelLeaveEvent) {
            val channel = (trigger as UserVoiceChannelLeaveEvent).voiceChannel
            if (channel.isConnected && channel.connectedUsers.count() == 1) {
                channel.leave()
                (trigger as UserVoiceChannelLeaveEvent).guild.defaultChannel.sendMessage(EmbedBuilder().info("left channel: all people left").build()).autoDelete(5)
            }
        }
        if (trigger is MessageReceivedEvent){
            if((trigger as MessageReceivedEvent).message.content == "you tried"){
                val response = arrayOf(
                        "Looking at this code, I'm sure ${(trigger as MessageReceivedEvent).client.applicationOwner.mention(true)} didn't",
                        "I'll do better next time... maybe... probably not since i'm a program",
                        "A program is only as good as its programmer",
                        "Sometimes I can't find my main class"
                ).random()
                RequestBuffer.request { (trigger as MessageReceivedEvent).channel.sendMessage(EmbedBuilder().withDesc(response).build()) }
            }
        }
    }

}