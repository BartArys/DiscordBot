package com.numbers.discordbot.commands

import com.numbers.discordbot.Command
import com.numbers.discordbot.commands.util.document
import com.numbers.discordbot.commands.util.withDefaultColor
import com.numbers.discordbot.filter.MessageFilter
import com.numbers.discordbot.loader.CommandLoader
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.util.EmbedBuilder

@Command(name =  "Documentation")
class DocumentationCommand{

    @Command
    @MessageFilter( eventType = MessageEvent::class, prefixCheck = true, startsWith = "docs", readableUsage = "docs")
    fun document(event: MessageEvent){
        val classes = CommandLoader().getClasses(Command::class.java, "com.numbers.discordbot.commands")

        val ordered = classes.associateBy ({ it }, {it.methods.filter { it.isAnnotationPresent(MessageFilter::class.java) }.map { it.getAnnotation(MessageFilter::class.java) } } )

        val description = ordered.entries.joinToString("\n") { "**[${it.key.getAnnotation(Command::class.java).name}](#)** \n\t${it.value.joinToString (" \n\t") { it.document() }}" }

        val builder = EmbedBuilder().withDefaultColor()

        event.channel.sendMessage(builder.appendDesc(description).build())
    }

}