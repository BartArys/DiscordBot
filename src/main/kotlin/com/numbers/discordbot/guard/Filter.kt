package com.numbers.discordbot.guard

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.MessageTokenizer
import java.util.*
import java.util.regex.Pattern
import kotlin.math.min

interface FilterItem {

    val rangeCheck : Int

    val minRangeCheck : Int get() = rangeCheck

    fun apply(tokens: Iterable<MessageTokenizer.Token>, args: MutableMap<String, String>) : Boolean

    fun flatMap() : Iterable<FilterItem>{
        return listOf(this)
    }
}

class Filter (val filters: List<Pair<FilterRange,FilterItem>>, val command: IListener<MessageReceivedEvent>) : IListener<MessageReceivedEvent>{

    private fun MessageTokenizer.nextToken() : MessageTokenizer.Token? {
        return when{
            this.hasNextMention() -> this.nextMention()
            this.hasNextInvite() -> this.nextInvite()
            this.hasNextEmoji() -> this.nextEmoji()
            this.hasNextInvite() -> this.nextInvite()
            this.hasNextWord() -> this.nextWord()
            else -> null
        }
    }

    private fun MessageTokenizer.hasNextToken() : Boolean {
        return when{
            this.hasNextMention() -> true
            this.hasNextInvite() -> true
            this.hasNextEmoji() -> true
            this.hasNextInvite() -> true
            this.hasNextWord() -> true
            else -> false
        }
    }

    private fun MessageTokenizer.allTokens() : List<MessageTokenizer.Token>{
        val tokens = mutableListOf<MessageTokenizer.Token>()
        while (hasNextToken())  tokens.add(nextToken()!!)
        return tokens
    }

    override fun handle(event: MessageReceivedEvent) {
        val tokens = event.message.tokenize().allTokens()
        val maxIndex = filters.map {  it.first.indexStart + it.second.minRangeCheck  }.max() ?: 0

        if(tokens.count() < maxIndex) return

        val args = mutableMapOf<String,String>()
        if (filters.all {
            val startIndex = it.first.indexStart
            var endIndex = startIndex + it.first.range
            val minEndIndex = startIndex + it.second.minRangeCheck
            if(tokens.size in minEndIndex..endIndex){
                endIndex = tokens.size
            }


            val subTokens = tokens.subList(startIndex, endIndex)

            it.second.apply(subTokens, args)
        }) {
            command.handle(event)
        }
    }

}

class ArgFilter (val filters: List<Pair<FilterRange,FilterItem>>, val command: IArgsListener) : IListener<MessageReceivedEvent> {

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
        val tokens = event.message.tokenize().allTokens()
        val maxIndex = filters.map { it.first.indexStart + it.second.rangeCheck }.max() ?: 0
        val minIndex = filters.map { it.first.indexStart + it.second.minRangeCheck }.max() ?: 0

        if(tokens.count() !in minIndex..maxIndex) return

        val args = mutableMapOf<String,String>()
        args["tokenCount"] = tokens.size.toString()
        if (filters.all {
            val startIndex = it.first.indexStart
            val endIndex = startIndex + it.first.range

            val subTokens = tokens.subList(startIndex, min(endIndex, tokens.count()))

            it.second.apply(subTokens, args)
        }) {
            command.handle(event, args)
        }
    }

}


data class FilterRange(val indexStart : Int, val range : Int = 1)

interface IArgsListener{

    fun handle(event: MessageReceivedEvent, args : Map<String,String>)

}

class FilterBuilder{

    private val filters = LinkedList<Pair<FilterRange,FilterItem>>()

    val items : Iterable<FilterItem> get() = filters.map { it.second }

    var index = 0

    fun pop() : FilterItem{
        return filters.removeLast().second.also { index -= it.rangeCheck }
    }

    fun addFilter(item: FilterItem){
        filters += FilterRange(index, item.rangeCheck) to item
        index += item.rangeCheck
    }

    private fun Iterable<FilterItem>.range() : Int{
        val max = this.map { it.minRangeCheck }.max()!!
        val min = this.map { it.minRangeCheck }.min()!!

        if(max != min) throw IllegalArgumentException("range for filters must be equal")
        return this.map { it.rangeCheck }.max()!!

    }

    fun addOrFilters(items: Iterable<FilterItem>){
        val combined = CombinedOrFilterItem(items.range(), items)
        addFilter(combined)
    }

    /*
    fun addAndFilters(items : Iterable<FilterItem>){
        filters += FilterRange(index, items.range()) to CombinedAndFilterItem(items.range(), items)
    }*/

    fun build(command: IListener<MessageReceivedEvent>) : IListener<MessageReceivedEvent>{
        return Filter(filters, command)
    }

    fun build(handler : (MessageReceivedEvent, Map<String,String>) -> Unit) : IListener<MessageReceivedEvent>{
        return ArgFilter(filters, object : IArgsListener{
            override fun handle(event: MessageReceivedEvent, args: Map<String, String>) {
                handler(event, args)
            }
        })
    }

    private class CombinedAndFilterItem(override val rangeCheck : Int, items: Iterable<FilterItem>) : FilterItem{

        private val items : Iterable<FilterItem> = items.flatMap { it.flatMap() }

        override fun apply(tokens: Iterable<MessageTokenizer.Token>, args: MutableMap<String, String>): Boolean = items.all { it.apply(tokens, args) }

        override fun flatMap(): Iterable<FilterItem> = items

    }

    private class CombinedOrFilterItem(override val rangeCheck : Int, items: Iterable<FilterItem>) : FilterItem{

        override val minRangeCheck: Int
            get() = items.map { it.minRangeCheck }.max()!!


        val items : Iterable<FilterItem> = items.flatMap { it.flatMap() }

        override fun apply(tokens: Iterable<MessageTokenizer.Token>, args: MutableMap<String, String>): Boolean = items.any { it.apply(tokens, args) }

        override fun flatMap(): Iterable<FilterItem> = items.flatMap {  (it as? CombinedOrFilterItem)?.items ?: listOf(it) }
    }

}

class WordFilterItem(val word: String) : FilterItem{
    override val rangeCheck: Int = 1

    override fun apply(tokens: Iterable<MessageTokenizer.Token>, args: MutableMap<String, String>): Boolean = tokens.first().content == word

}

class ArgumentFilterItem(val key : String, val type: ArgumentType, override val rangeCheck: Int = 1, override val minRangeCheck: Int = rangeCheck) : FilterItem{

    override fun apply(tokens: Iterable<MessageTokenizer.Token>, args: MutableMap<String, String>): Boolean {
        if(type.matches(tokens)){
            args[key] = type.toString(tokens)
            return true
        }
        return false
    }

}

enum class ArgumentType(val matches : (Iterable<MessageTokenizer.Token>) -> Boolean, val toString : (Iterable<MessageTokenizer.Token>) -> String){

    BOOLEAN({ tokens -> tokens.first().content.toLowerCase().matches(kotlin.text.Regex("true|false")) }, { it.first().content }),
    INT({ tokens -> tokens.first().content.toIntOrNull() != null }, { it.first().content }),
    WORD({ _ -> true }, { it.joinToString(separator = " ") { it.content } }),
    WORDS({ _ -> true }, { it.joinToString(separator = " ") { it.content } }),
    USER_MENTION({ token -> token.first() is MessageTokenizer.UserMentionToken }, { (it.first() as sx.blah.discord.util.MessageTokenizer.UserMentionToken).mentionObject.stringID }),
    CHANNEL_MENTION({ token -> token.first() is MessageTokenizer.ChannelMentionToken }, { (it.first() as sx.blah.discord.util.MessageTokenizer.ChannelMentionToken).mentionObject.stringID }),
    TEXT_CHANNEL_MENTION({ token -> token.first() is MessageTokenizer.ChannelMentionToken && (token.first() as sx.blah.discord.util.MessageTokenizer.ChannelMentionToken).mentionObject as? sx.blah.discord.handle.obj.IVoiceChannel == null }, { (it.first() as sx.blah.discord.util.MessageTokenizer.ChannelMentionToken).mentionObject.stringID }),
    // VOICE_CHANNEL_MENTION({ token -> token.first() is MessageTokenizer.ChannelMentionToken && (token.first() as MessageTokenizer.ChannelMentionToken).mentionObject as? sx.blah.discord.handle.obj.IVoiceChannel != null }, { (it.first() as sx.blah.discord.util.MessageTokenizer.ChannelMentionToken).mentionObject.stringID }),
    URL({ token -> org.apache.commons.validator.routines.UrlValidator.getInstance().isValid(token.first().content) }, { it.joinToString(separator = " ") { it.content } });




}