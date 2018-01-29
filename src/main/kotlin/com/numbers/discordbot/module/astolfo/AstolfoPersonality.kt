package com.numbers.discordbot.module.astolfo

import com.numbers.discordbot.personality.Personality
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.util.EmbedBuilder

class AstolfoPersonality : Personality{

    private fun astolfoBase() : EmbedBuilder{
        return EmbedBuilder().withColor(251,202,184)
    }

    override fun alreadyInChannel(voiceChannel: IVoiceChannel): EmbedBuilder {
        return astolfoBase().withDesc("I'm already in the voice channel, dummy").withImage(ReactionImages.ANNOYED.urls.random())
    }

    override fun voiceChannelJoin(voiceChannel: IVoiceChannel): EmbedBuilder {
        return astolfoBase().withDesc("I'm already in the voice channel, dummy").withImage(ReactionImages.MUSIC_START.urls.random())
    }

}