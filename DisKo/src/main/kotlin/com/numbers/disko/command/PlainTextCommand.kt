package com.numbers.disko.command

import com.numbers.disko.Command
import com.numbers.disko.CommandArguments
import com.numbers.disko.CommandContext
import com.numbers.disko.SetupContext
import com.numbers.disko.permission.PermissionSupplier
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

internal data class PlainTextCommand(val command: Command, private val supplier: PermissionSupplier) : IListener<MessageReceivedEvent> {

    override fun handle(event: MessageReceivedEvent) {
        if(!command.permissions.all { permission -> supplier.forUser(event.author).any { permission.isAssignableFrom(it::class.java) } }) return

        if (command.usage == event.message.content) {
            val context = CommandContext(services = SetupContext.sharedContext.services, args = CommandArguments.empty, event = event)
            launch {
                SetupContext.sharedContext.services.context = context
                command.handler!!.invoke(context)
            }
        }
    }

}