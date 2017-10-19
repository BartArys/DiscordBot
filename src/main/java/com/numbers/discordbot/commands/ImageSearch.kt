package com.numbers.discordbot.commands

import com.numbers.discordbot.Command
import com.numbers.discordbot.commands.util.skipNextWord
import com.numbers.discordbot.commands.util.withDefaultColor
import com.numbers.discordbot.filter.MessageFilter
import com.numbers.jttp.Jttp
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.util.EmbedBuilder
import java.net.URLEncoder
import java.util.*

@Command(name = "Image Search")
class ImageSearch{

    fun searchPrefix(){

    }

    @Command
    @MessageFilter(eventType = MessageEvent::class, readableUsage = "image \$search", regex = "image\\s.*", prefixCheck = true)
    fun searcMention(event: MessageEvent, jttp: Jttp){
        search(event = event, jttp = jttp, search = event.message.tokenize().skipNextWord().skipNextWord().remainingContent.trim())
    }

    fun search(event : MessageEvent, jttp: Jttp, search : String){
        val list : ArrayList<String> = jttp.get("http://localhost:8080/reddit/googleImages/" + URLEncoder.encode(search, "UTF-8")).asObjects(java.util.ArrayList::class.java, String::class.java).join().response as ArrayList<String>

        event.channel.sendMessage(EmbedBuilder().withDefaultColor().withImage(list.first()).build())
    }

}