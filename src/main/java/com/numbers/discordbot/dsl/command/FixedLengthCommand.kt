package com.numbers.discordbot.dsl.command

import com.numbers.discordbot.dsl.*
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.MessageTokenizer
import java.util.regex.Pattern

class FixedLengthCommand(items: List<FilterItem>, val command: Command) : IListener<MessageReceivedEvent> {

    private val indexedItems = items.mapIndexed { index, filterItem -> index to filterItem }

    private fun MessageTokenizer.nextToken() : MessageTokenizer.Token? {
        return when{
            this.hasNextMention()
                    && hasNextToken(MessageTokenizer.ANY_MENTION_PATTERN) -> this.nextMention()
            this.hasNextInvite()
                    && hasNextToken(MessageTokenizer.INVITE_PATTERN) -> this.nextInvite()
            this.hasNextEmoji()
                    && hasNextToken(MessageTokenizer.CUSTOM_EMOJI_PATTERN) -> this.nextEmoji()
            this.hasNextWord() -> this.nextWord()
            else -> null
        }
    }

    private fun MessageTokenizer.hasNextToken() : Boolean {
        return when{
            this.hasNextMention() -> true
            this.hasNextInvite() -> true
            this.hasNextEmoji() -> true
            this.hasNextWord() -> true
            else -> false
        }
    }

    private fun MessageTokenizer.hasNextToken(pattern: Pattern) : Boolean {
        val matcher = pattern.matcher(remainingContent.trim())
        if (!matcher.find()) return false

        val start = 0
        // val end = remainingContent.length

        //val matcherEnd = matcher.end()
        val matcherStart = matcher.start()

        return matcherStart == start

    }

    private fun MessageTokenizer.allTokens() : List<MessageTokenizer.Token>{
        val tokens = mutableListOf<MessageTokenizer.Token>()
        while (hasNextToken())  tokens.add(nextToken()!!)
        return tokens
    }

    override fun handle(event: MessageReceivedEvent) {
        val args = CommandArguments()
        if(event.message.content.isNullOrBlank()) return

        val tokens = event.message.tokenize().allTokens().map { Token(event.client, it.content) }
        if (tokens.size != indexedItems.size) return

        if(indexedItems.all { it.second.apply(listOf(tokens[it.first]), event, services, args) }) {
            val context = CommandContext( services = services, args = args, event = event )
            launch {
                services.context = context
                command.handler!!.invoke(context)
            }
        }
    }

}