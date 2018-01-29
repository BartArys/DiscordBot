package com.numbers.discordbot.action

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.PrefixService
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RequestBuffer

class SetPrefixAction {

    @Guards("""
        sets the new personal prefix

    """,Guard("set prefix {prefix}", Argument(ArgumentType.WORD, "the new prefix")))
    fun setPrefix(event: MessageReceivedEvent, args: CommandArguments, personality: Personality, prefixService: PrefixService){
        prefixService.setPrefix(event.author, args["prefix"]!!)
        event.message.autoDelete()
        RequestBuffer.request { event.channel.sendMessage(personality.newPrefix(event.author, args["prefix"]!!).build()).autoDelete() }
    }

}