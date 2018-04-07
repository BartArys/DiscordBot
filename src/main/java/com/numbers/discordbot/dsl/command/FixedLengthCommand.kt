package com.numbers.discordbot.dsl.command

import com.numbers.discordbot.dsl.*
import com.numbers.discordbot.dsl.permission.PermissionSupplier
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

class FixedLengthCommand(items: Array<FilterItem>, val command: Command, val supplier: PermissionSupplier) : IListener<MessageReceivedEvent> {

    private val indexedItems = items.mapIndexed { index, filterItem -> index to filterItem }.toTypedArray()

    override fun handle(event: MessageReceivedEvent) {
        if(!command.permissions.all { permission -> supplier.forUser(event.author).any { permission.isAssignableFrom(it::class.java) } }) return

        val args = CommandArguments()
        if (event.message.content.isNullOrBlank()) return

        val tokens = event.message.tokenize().allTokens().map { Token(event.client, it.content) }
        if (tokens.size != indexedItems.size) return

        if (indexedItems.all { it.second.apply(listOf(tokens[it.first]), event, SetupContext.sharedContext.services, args) }) {
            val context = CommandContext(services = SetupContext.sharedContext.services, args = args, event = event)
            launch {
                command.handler!!.invoke(context)
            }
        }
    }

}