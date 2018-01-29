package com.numbers.discordbot.guard2

import com.numbers.discordbot.service.PrefixService
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.MessageTokenizer
import java.util.regex.Pattern
import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.math.min

class Filter<T>(private var items : MutableList<Pair<Int, FilterItem>> = mutableListOf()) where T : MessageEvent{

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

    private fun Pair<Int,FilterItem>.toRange(max: Int) : Pair<Pair<Int,Int>,FilterItem>{
        val minMax = this.first + this.second.minLength

        if(minMax > max) throw IllegalArgumentException("out of range")

        if(minMax < max){
            val maxMax = this.first + this.second.maxLength
            return (this.first to min(max, maxMax)) to this.second
        }
        return (this.first to minMax) to this.second
    }

    infix fun bindTo(block: (MessageReceivedEvent, CommandArguments) -> Unit) : IListener<MessageReceivedEvent> {
        return IListener { event ->
                val args = CommandArguments(event.client)
                val tokens = event.message.tokenize().allTokens().map { Token(event.client, it.content) }
                args["tokenCount"] = tokens.size

                val maxIndex = items.map { it.first + it.second.maxLength }.max() ?: 0
                val minIndex = items.map { it.first + it.second.minLength }.max() ?: 0

                if (tokens.size > maxIndex || tokens.size < minIndex) return@IListener

                launch {
                    val ranges = items.map { it.toRange(tokens.size) }
                    if(ranges.all {
                                it.second.apply(event, tokens.subList(it.first.first, it.first.second), args)
                            }){
                        block(event, args)
                    }
                }
        }
    }

}

class FilterBuilder{

    private var items = mutableListOf<Pair<Int, FilterItem>>()
    private val index : Int get() { return items.map { it.first }.max() ?: -1}

    val nextIndex : Int get() { return index+1 }

    fun build() : Filter<MessageReceivedEvent>{
        return Filter(items)
    }

    fun insert(at: Int, item: FilterItem){
        items.add(at to item)
    }

    fun remove(at: Int) : Iterable<FilterItem>{
        val removed = items.filter { it.first == at }
        items = (items - removed).toMutableList()
        return removed.map { it.second }
    }

}


abstract class FilterItem{

    open val minLength : Int = 1
    open val maxLength : Int get() { return minLength }

    abstract suspend fun apply(event: MessageEvent, tokens: Iterable<Token>, args: CommandArguments) : Boolean

}

class OptionalFilterItem(val item: FilterItem) : FilterItem(){
    override val minLength: Int = 0
    override val maxLength: Int get() = item.maxLength

    override suspend fun apply(event: MessageEvent, tokens: Iterable<Token>, args: CommandArguments): Boolean {
        if(tokens.count() < item.minLength) return true

        return item.apply(event,tokens,args)
    }
}

class OrFilterItem(val items: Iterable<FilterItem>) : FilterItem(){

    override val minLength: Int get() = items.map { it.minLength }.min() ?: 0
    override val maxLength: Int get() = items.map { it.maxLength }.max() ?: 0

    override suspend fun apply(event: MessageEvent, tokens: Iterable<Token>, args: CommandArguments): Boolean {
        return items.any { it.apply(event, tokens.toList().subList(0, min(tokens.count(), it.maxLength)), args) }
    }


}

class TextSequenceItem(text : String) : FilterItem(){

    private val words : List<String> = text.split(" ")

    override val minLength: Int get() = words.size
    override val maxLength: Int get() = minLength

    override suspend fun apply(event: MessageEvent, tokens: Iterable<Token>, args: CommandArguments): Boolean {
        return tokens.count() == words.size && tokens.mapIndexed { index, token -> index to token }.all { words[it.first] == it.second.content }
    }

}


open class PrefixItem(protected val prefixService: PrefixService) : FilterItem(){

    override suspend fun apply(event: MessageEvent, tokens: Iterable<Token>, args: CommandArguments): Boolean = suspendCoroutine {
        if (tokens.count() != 1) it.resume(false)

        launch {
            val userPrefix = prefixService.getPrefix(event.author)
            val cont = userPrefix == tokens.first().content
            it.resume(cont)
        }
    }

}

open class PrefixSuffixItem(prefixService: PrefixService, private val suffix: String) : PrefixItem(prefixService){

    override suspend fun apply(event: MessageEvent, tokens: Iterable<Token>, args: CommandArguments): Boolean = suspendCoroutine {
        if (tokens.count() != 1) it.resume(false)

        val content = tokens.first().content
        if(!content.endsWith(suffix)) {
            it.resume(false)
            return@suspendCoroutine
        }

        launch {
            val userPrefix = prefixService.getPrefix(event.author)
            val cont = userPrefix == tokens.first().content.removeSuffix(suffix)
            it.resume(cont)
        }
    }

}

class ArgumentItem(val type: ArgumentType, val key : String) : FilterItem(){

    override val minLength: Int get() = type.minLength
    override val maxLength: Int get() = type.maxLength

    override suspend fun apply(event: MessageEvent, tokens: Iterable<Token>, args: CommandArguments): Boolean = type.apply(event, tokens, args, key)

}

class PaddedArgumentItem(private val type: ArgumentType, private val key : String, private val prefix: String = "", private val suffix: String = "") : FilterItem(){

    override val minLength: Int get() = type.minLength
    override val maxLength: Int get() = type.maxLength

    override suspend fun apply(event: MessageEvent, tokens: Iterable<Token>, args: CommandArguments): Boolean{

        var tokens = tokens.toMutableList()

        if(! tokens.first().content.startsWith(prefix)) return false
        if(! tokens.last().content.endsWith(suffix)) return false

        if(tokens.count() == 1){
            val content = tokens.first().content.removePrefix(prefix).removeSuffix(suffix)
            val first = Token(event.client, content)
            return type.apply(event, listOf(first), args, key)
        }

        val firstContent = tokens.first().content.removePrefix(prefix)

        val first = if(firstContent.isEmpty()){
            tokens.removeAt(0)
            tokens.toList()[0]
        }else{
            Token(event.client, firstContent)
        }
        tokens.removeAt(0)


        val lastContent = tokens.last().content.removeSuffix(suffix)

        val last = if(lastContent.isEmpty()){
            tokens.removeAt(tokens.size - 1)
            tokens.last()
        }else{
            Token(event.client, lastContent)
        }
        tokens.removeAt(tokens.size - 1)

        tokens.add(0, first)
        tokens.add(last)

        return type.apply(event, tokens, args, key)
    }

}


class WordSequenceItem(private val word: String) : FilterItem(){

    override suspend fun apply(event: MessageEvent, tokens: Iterable<Token>, args: CommandArguments): Boolean {
        return tokens.count() == 1 && tokens.first().content == word
    }

}

enum class ArgumentType(val minLength: Int, val maxLength: Int = minLength, val apply: (event: MessageEvent, tokens: Iterable<Token>, args: CommandArguments, key : String) -> Boolean) {

    INT(1, apply = { _, tokens, args, key ->
        tokens.first().isInt.andIfTrue {
            args[key] = tokens.first().content.toInt()
        }

    }),
    BOOLEAN(1, apply =  { _, tokens, args, key ->
        tokens.first().isBoolean.andIfTrue {
            args[key] = tokens.first().content.toBoolean()
        }
    }),

    WORD(1, apply = { _, tokens, args, key ->  true.also { args[key] = tokens.first().content } }),
    WORDS(1, 2000, { _, tokens, args, key ->  true.also { args[key] = tokens.joinToString(separator = " ") { it.content } } }),

    USER_MENTION(1, apply = { event, tokens, args, key ->
        tokens.first().isUserMention.andIfTrue {
            args[key] = event.client.getUserByID(tokens.first().content.removePrefix("<@").removePrefix("!").removeSuffix(">").toLong())
        }
    }),
    TEXT_CHANNEL_MENTION(1, apply = { event , tokens, args, key ->
        tokens.first().isTextChannelMention.andIfTrue {
            args[key] = event.client.getChannelByID(tokens.first().content.removePrefix("<@").removeSuffix(">").toLong())
        }
    }),
    VOICE_CHANNEL_MENTION(1, apply =  { event , tokens, args, key ->
        tokens.first().isVoiceChannelMention.andIfTrue {
            args[key] = event.client.getChannelByID(tokens.first().content.removePrefix("<@").removeSuffix(">").toLong())
        }
    }),
    URL(1, apply = { _, tokens, args, key ->
        org.apache.commons.validator.routines.UrlValidator.getInstance().isValid(tokens.first().content).andIfTrue {
            args[key] = tokens.first().content
        }
    });

}

internal inline fun kotlin.Boolean.andIfTrue(block: (Boolean) -> Unit) : Boolean{
    if (this) block(this)
    return this
}