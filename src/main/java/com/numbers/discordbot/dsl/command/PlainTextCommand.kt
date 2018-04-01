package com.numbers.discordbot.dsl.command

import com.numbers.discordbot.dsl.Command
import com.numbers.discordbot.dsl.CommandArguments
import com.numbers.discordbot.dsl.CommandContext
import com.numbers.discordbot.dsl.SetupContext
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

data class PlainTextCommand(val command: Command) : IListener<MessageReceivedEvent> {

    override fun handle(event: MessageReceivedEvent) {
        if (command.usage == event.message.content) {
            val context = CommandContext(services = SetupContext.sharedContext.services, args = CommandArguments.empty, event = event)
            launch {
                SetupContext.sharedContext.services.context = context
                command.handler!!.invoke(context)
            }
        }
    }

}