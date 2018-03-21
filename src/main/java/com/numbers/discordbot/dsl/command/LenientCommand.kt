package com.numbers.discordbot.dsl.command

import com.numbers.discordbot.dsl.*
import kotlinx.coroutines.experimental.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.MessageTokenizer
import java.util.regex.Pattern
import kotlin.math.min

class LenientCommand(items: List<FilterItem>,val command: Command) : IListener<MessageReceivedEvent>{

    private val items = items.map { filterItem ->  IndexedFilterItem(filterItem.minLength..filterItem.maxLength, filterItem) }

    data class IndexedFilterItem(val acceptableRange : IntRange, val item: FilterItem) : FilterItem by item

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

    override fun handle(event: MessageReceivedEvent) {
        if(event.message.content.isNullOrBlank()) return

        val tokens = event.message.tokenize().allTokens().map { Token(event.client, it.content) }
        val args = CommandArguments()
        var tokenIndex = 0

        logger.debug("{}: starting matching attempt", command.usage)

        var i = 0
        while (i < items.size){
            fun isLastItem() : Boolean{
                return i == items.size -1
            }
            logger.trace("{}: current filterItem is {}, current token is {}", command.usage , items[i], tokens[tokenIndex])
            val indexedItem = items[i]
            if(indexedItem.item.isVararg){
                var currentSize = min(indexedItem.maxLength, tokens.size - tokenIndex)
                while(currentSize in indexedItem.acceptableRange){
                    if(isLastItem() && indexedItem.apply(tokens.subList(tokenIndex, tokenIndex + currentSize), event, services, args)){
                        logger.trace("{}; last item vararg matched", command.usage)
                        execute(args, event)
                        return
                    }else if(
                        indexedItem.apply(tokens.subList(tokenIndex, tokenIndex + currentSize), event, services, args)
                        && items[i+1].apply(listOf(tokens[tokenIndex + currentSize - 1]), event, services, args)
                    ){
                        i++
                        logger.trace("{}: vararg and next item matched", command.usage)
                    }
                    currentSize--
                }
                if(currentSize !in indexedItem.acceptableRange){
                    logger.trace("{}: tokens exhausted, no match", command.usage)
                    return
                }
            }else if(!indexedItem.apply(listOf(tokens[tokenIndex]), event, services, args)){
                logger.trace("{}: 1-length token didn't match, ignoring command", command.usage)
                return
            }else{
                tokenIndex++
                logger.trace("{}: increasing tokenIndex to {}", command.usage, tokenIndex)
            }
            i++
            logger.trace("{}: moving on to filterItem {}", command.usage, i)
        }

        execute(args, event)
    }

    private fun execute(args: CommandArguments, event: MessageReceivedEvent){
        val context = CommandContext( services = services, args = args, event = event )
        launch {
            services.context = context
            command.handler!!.invoke(context)
        }
    }

    companion object {
        val logger : Logger = LoggerFactory.getLogger(LenientCommand::class.java)
    }

}