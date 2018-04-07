package com.numbers.discordbot.dsl.command

import com.numbers.discordbot.dsl.*
import com.numbers.discordbot.dsl.permission.PermissionSupplier
import kotlinx.coroutines.experimental.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.util.*
import kotlin.math.min

class LenientCommand(val items: Array<FilterItem>, val command: Command, val supplier: PermissionSupplier) : IListener<MessageReceivedEvent> {

    override fun handle(event: MessageReceivedEvent) {
        if (event.message.content.isNullOrBlank()) return
        if(!command.permissions.all { permission -> supplier.forUser(event.author).any { permission.isAssignableFrom(it::class.java) } }) return

        val tokens = event.message.tokenize().allTokens().map { Token(event.client, it.content) }
        val args = CommandArguments()
        val items: Queue<FilterItem> = LinkedList(items.toList())
        var tokenIndex = 0
        logger.debug("{}: starting matching attempt", command.usage)

        while (!items.isEmpty() && tokenIndex < tokens.size) {
            val filterItem = items.poll()
            logger.trace("{}: starting matching attempt for filter item: {}", command.usage, filterItem)
            if (filterItem.isVararg) {
                logger.trace("{}: recognized {} as vararg: ", command.usage, filterItem)
                var maxIndex = min(tokenIndex + filterItem.length.last + 1, tokens.size) //max index exclusive
                while (maxIndex in filterItem.length) {
                    logger.trace("{}: starting matching attempt for tokens {}: ", command.usage, tokens.subList(tokenIndex, maxIndex).joinToString(" ", "[ ", " ]"))
                    if (items.isEmpty() && filterItem.apply(tokens.subList(tokenIndex, maxIndex), event, SetupContext.sharedContext.services, args)) {
                        logger.trace("{}: last vararg matched for {}, executing command", command.usage, tokens.subList(tokenIndex, maxIndex).joinToString(" ", "[ ", " ]"))
                        execute(args, event)
                        return
                    } else if (
                            filterItem.apply(tokens.subList(tokenIndex, maxIndex), event, SetupContext.sharedContext.services, args)
                            && items.peek().apply(listOf(tokens[min(maxIndex, tokens.size - 1)]), event, SetupContext.sharedContext.services, args)
                    ) {
                        logger.trace("{}: vararg {} matched as well as next item {}, executing command", command.usage, filterItem, items.peek())
                        tokenIndex = min(maxIndex, tokens.size - 1) + 1 // set index to last matched index
                        items.poll() // remove next item since it also matched
                        break
                    }
                    maxIndex--
                }
                if (maxIndex !in filterItem.length) {
                    return
                }
            } else if (filterItem.apply(listOf(tokens[tokenIndex]), event, SetupContext.sharedContext.services, args)) {
                tokenIndex++
            } else {
                return
            }
        }

        execute(args, event)
    }

    private fun execute(args: CommandArguments, event: MessageReceivedEvent) {
        val context = CommandContext(services = SetupContext.sharedContext.services.copy(), args = args, event = event)
        logger.debug("calling command {}", command.info)
        launch {
            command.handler!!.invoke(context)
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(LenientCommand::class.java)
    }

}