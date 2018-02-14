package com.numbers.discordbot.personality

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.error
import com.numbers.discordbot.extensions.info
import com.numbers.discordbot.module.music.Track
import com.numbers.discordbot.network.EightBallResponse
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.util.EmbedBuilder
import java.awt.Color
import java.util.*

class DeusVultPersonality : Personality {
    override fun voiceChannelJoin(voiceChannel: IVoiceChannel): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("I Have joined ${voiceChannel.name} to lend my aid in the holy fight!")
    }

    override fun voiceChannelLeave(voiceChannel: IVoiceChannel): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("I will stand down")
    }

    override fun voiceChannelLeaveAlone(voiceChannel: IVoiceChannel): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("My brothers have forsaken me!")

    }

    override fun alreadyInChannel(voiceChannel: IVoiceChannel): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("Are you ill? I'm already partaking in your holy crusade!")
    }

    override fun failedToJoinChannel(): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("God had forsaken me, I cannot join you")
    }

    override fun songFound(track: Track): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("Some martial music will scare the saracens, I call this one ${track.identifier}. A nice addition indeed")
    }

    override fun songLoadFailed(exception: Exception): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("I cannot complete your request. A vision came to me, it said: ${exception.localizedMessage}")
    }

    override fun noMatches(retrying: Boolean, search: String?): EmbedBuilder {
        return if (retrying) {
            EmbedBuilder().withColor(Color.YELLOW).withDesc("A song with that name? Never heard of it, allow me to access the Holy library in a final attempt")
        } else {
            EmbedBuilder().withColor(Color.YELLOW).withDesc("I have done my very best, but i have not found such a song")
        }
    }

    override fun playlistFound(playlist: Iterable<Track>): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("Ah, a holy classic, I have added ${playlist.count()} martial songs to our crusade! This will surely bring terror to the saracens")
    }

    override fun skipSongs(amount: Int): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("There's no accounting for taste. Very well, i'll skip these ones")

    }

    override fun skipAll(): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("Ah, you're the sneaky type, eh? Very well...")
    }

    override fun numberRequired(): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("I expected a number there, you do know what a number is, right?")

    }

    override fun emptyQueue(): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("Are you delusional? There's no music being played")
    }

    override fun switchedTo(): EmbedBuilder {
        return EmbedBuilder().withColor(Color.YELLOW).withDesc("My Holy brothers, I have joined you in battle!")
    }

    override fun eightBall(eightBallResponse: EightBallResponse): EmbedBuilder {
        if (eightBallResponse.question.contains("loli") || eightBallResponse.question.contains("oppai") || eightBallResponse.question.contains("flat justice") || eightBallResponse.question.contains("flat chest") || eightBallResponse.question.contains("small tits")) {
            val answers = arrayOf("FLAT IS JUSTICE", "BOOBS ARE JUST LUMPS OF FAT", "BEING FLAT CHESTED IS A STATUS SYMBOL", "FLAT IS JUSTICE, DO NOT FORGET!", "FLAT CHESTS MATTER")

            return EmbedBuilder().withColor(Color.YELLOW).withDesc(answers[Random().nextInt(answers.size)])
        }


        return if (eightBallResponse.type == "Affirmative") {
            EmbedBuilder().withColor(Color.YELLOW).withDesc("by the gods it is true!")
        } else {
            EmbedBuilder().error("${eightBallResponse.question}? I sure hope not!")
        }
    }

    override fun <T> trigger(trigger: T) {
        if (trigger is UserVoiceChannelLeaveEvent) {
            val channel = (trigger as UserVoiceChannelLeaveEvent).voiceChannel
            if (channel.isConnected && channel.connectedUsers.count() == 1) {
                channel.leave()
                (trigger as UserVoiceChannelLeaveEvent).guild.defaultChannel.sendMessage(EmbedBuilder().info("My brothers have forsaken me!").build()).autoDelete(5)
            }
        }
    }
}