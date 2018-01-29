package com.numbers.discordbot.commands

import com.numbers.discordbot.Command
import com.numbers.discordbot.commands.util.withDefaultColor
import com.numbers.discordbot.filter.MessageFilter
import com.numbers.jttp.Jttp
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.util.EmbedBuilder

@Command(name = "Random birb")
class RandomBirb{

    @Command
    @MessageFilter(eventType = MessageEvent::class, prefixCheck = true, readableUsage = "birb", regex = "birb")
    fun randomBirb(event: MessageEvent, jttp : Jttp){
        tweet(event, jttp)
    }

    fun tweet(event: MessageEvent, jttp: Jttp){
        val url = jttp.get("http://random.birb.pw/tweet/").asString().join().response

        val embed = EmbedBuilder().withDefaultColor().withImage("http://random.birb.pw/img/$url").build()
        event.channel.sendMessage(embed)
    }

}
