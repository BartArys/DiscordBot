package com.numbers.discordbot.dsl

import kotlinx.coroutines.experimental.launch
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.MessageTokenizer
import java.util.regex.Pattern
import kotlin.math.min

class CompiledCommand(val items: List<Pair<Int, FilterItem>>, val command: Command) : IListener<MessageReceivedEvent> {

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
        val matcherStart = matcher.start()

        return matcherStart == start

    }

    private fun MessageTokenizer.allTokens() : List<MessageTokenizer.Token>{
        val tokens = mutableListOf<MessageTokenizer.Token>()
        while (hasNextToken())  tokens.add(nextToken()!!)
        return tokens
    }

    private fun Pair<Int, FilterItem>.toRange(max: Int) : Pair<Pair<Int,Int>, FilterItem>{
        val minMax = this.first + this.second.minLength

        if(minMax > max) throw IllegalArgumentException("out of range")

        if(minMax < max){
            val maxMax = this.first + this.second.maxLength
            return (this.first to min(max, maxMax)) to this.second
        }
        return (this.first to minMax) to this.second
    }

    override fun handle(event: MessageReceivedEvent) {
        val args = CommandArguments()
        val tokens = if(event.message.content.isNullOrBlank()){
            emptyList()
        }else{
            event.message.tokenize().allTokens().map { Token(event.client, it.content) }
        }

        val maxIndex = items.map { it.first + it.second.maxLength }.max() ?: 0
        val minIndex = items.map { it.first + it.second.minLength }.max() ?: 0

        if (tokens.size > maxIndex || tokens.size < minIndex) return

        val ranges = items.map { it.toRange(tokens.size) }
        if(ranges.all { it.second.apply(tokens.subList(it.first.first, min(it.first.second, tokens.size)), event, services, args) }) {
            val context = CommandContext( services = services, args = args, event = event )
            launch {
                services.context = context
                command.handler!!.invoke(context)
            }
        }
    }

}

