package com.numbers.discordbot.commands

import com.numbers.discordbot.Command
import com.numbers.discordbot.commands.util.random
import com.numbers.discordbot.commands.util.withDefaultColor
import com.numbers.discordbot.filter.MessageFilter
import com.numbers.discordbot.network.reddit.ProgrammerHumor
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.util.EmbedBuilder

@Command(name =  "/r/ProgrammerHumor")
class ProgrammerHumorCommand {

    @Command
    @MessageFilter(eventType = MentionEvent::class, mentionsBot = true, regex = ".*programmerHumor", readableUsage = "programmerHumor")
    fun handleMention(event: MentionEvent, ph: ProgrammerHumor) {
        handlePrefix(event, ph)
    }

    @Command
    @MessageFilter(eventType = MessageEvent::class, prefixCheck = true, regex = "programmerHumor", readableUsage = "programmerHumor")
    fun handlePrefix(event: MessageEvent, ph: ProgrammerHumor){
        val post = ph.posts.random()
        val builder = EmbedBuilder().withDefaultColor().withImage(post.url).withUrl(post.url).withTitle(post.title)

        event.channel.sendMessage(builder.build())
    }

}