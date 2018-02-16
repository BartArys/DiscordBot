package com.numbers.discordbot.dsl

import kotlinx.coroutines.experimental.launch
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.MessageTokenizer
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern
import kotlin.math.min

class CommandCompiler (format : String, context: ArgumentContext, val command: Command, val services: Services) {

    private val context: NormalizingContext = context.normalized()
    private val normalizedFormat: String = format.normalized()

    private val special: CharArray = charArrayOf('{','|','}','?')
    private val tokens get() = context.tokenSubstitutes.keys.toCharArray()

    private fun ArgumentContext.normalized() : NormalizingContext{
        val argumentSubs : Map<String,Argument> = tokenSubstitutes.map { it.toString() to it.value }
                .map { it.second.toKeyedArguments().toMutableMap() }
                .reduce { acc, map -> (acc + map).toMutableMap() } + argumentSubstitutes

        return NormalizingContext(
                tokenSubstitutes = tokenSubstitutes,
                argumentSubstitutes = argumentSubs
        )
    }

    private fun String.normalized() : String{
        val cursor = CharCursor(this)
        val builder = StringBuilder()
        while(cursor.hasNext){
            builder.append(cursor.consumeUntil(*tokens))
            if(cursor.hasNext){
                val argumentKey = context.tokenSubstitutes[cursor.char]!!.toKeyedArguments().entries.first().key
                builder.append('{').append(argumentKey).append('}')
                cursor.next()
            }

        }
        return builder.toString()
    }

    private fun parse() : List<FilterItem> {
        val cursor = CharCursor(normalizedFormat)
        val queue = LinkedList<FilterItem>()
        cursor.consumeWhile(' ')
        while (cursor.hasNext){
            when(cursor.char){
                '{' -> {
                    cursor.next()
                    val content = cursor.consumeUntil(*special)
                    val arg = context.argumentSubstitutes[content]!!
                    queue.add(arg)
                }
                '|' -> {
                    cursor.next()
                    when {
                        cursor.char == '{' -> {
                            cursor.next()
                            val content = cursor.consumeUntil(*special)
                            val arg = context.argumentSubstitutes[content]!!
                            val prev = queue.removeLast()
                            queue.add(prev or arg)
                        }
                        cursor.findFirst(*special)?.let { it in charArrayOf('}', '|') } == true -> {
                            val content = cursor.consumeUntil(*special)
                            val item = context.argumentSubstitutes[content] ?: WordFilterItem(content)
                            queue.add(item)
                        }
                        else -> {
                            val arg = queue.removeLast()
                            queue.add(arg or WordFilterItem(cursor.consumeUntil(*special, ' ')))
                        }
                    }
                }
                '}' -> {
                    cursor.next()
                    if(cursor.hasNext && cursor.char !in special){
                        val suffix = cursor.consumeUntil(*special, ' ')
                        if(suffix.isNotEmpty() && suffix.isNotBlank()){
                            val arg = queue.removeLast() as Argument
                            queue.add(PaddedArgument(arg, "", suffix))
                        }
                    }
                }
                '?' -> {
                    cursor.next()
                    val opt = queue.removeLast().asOptional()
                    queue.add(opt)
                }
                else -> {
                    val content = cursor.consumeUntil(*special , ' ')
                    if(cursor.hasNext && cursor.char == '{'){
                        cursor.next()
                        var arg = context.argumentSubstitutes[cursor.consumeUntil(*special)]!!
                        while (cursor.char != '}'){
                            cursor.next()
                            arg = arg or context.argumentSubstitutes[cursor.consumeUntil(*special)]!!
                        }
                        cursor.next()
                        val suffix = cursor.consumeUntil(' ')
                        queue.add(PaddedArgument(arg, content, suffix))
                    }else{
                        queue.add(WordFilterItem(content))
                    }
                }
            }
            cursor.consumeWhile(' ')
        }
        return queue
    }

    private fun CharCursor.findFirst(vararg  special : Char) : Char?{
        val copy = this.copy(content = content, counter = java.util.concurrent.atomic.AtomicInteger(counter.get()))
        while (copy.hasNext){
            when(copy.char){
                in special -> return copy.char
            }
            copy.next()
        }
        return null
    }

    private fun CharCursor.consumeWhile(vararg  special : Char) : String{
        val builder = StringBuilder()
        while (hasNext && special.contains(char)){
            builder.append(char)
            next()
        }
        return builder.toString()
    }

    private fun CharCursor.consumeUntil(vararg special: Char) : String {
        val builder = StringBuilder()
        while(hasNext && !special.contains(char)){
            builder.append(char)
            next()
        }
        return builder.toString()
    }

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

    private fun Pair<Int, FilterItem>.toRange(max: Int) : Pair<Pair<Int,Int>, FilterItem>{
        val minMax = this.first + this.second.minLength

        if(minMax > max) throw IllegalArgumentException("out of range")

        if(minMax < max){
            val maxMax = this.first + this.second.maxLength
            return (this.first to min(max, maxMax)) to this.second
        }
        return (this.first to minMax) to this.second
    }

    operator fun invoke() : IListener<MessageReceivedEvent>{
        val items = parse().mapIndexed { index, filterItem ->  index  to filterItem }
        return IListener {event ->
            val args = CommandArguments(event.client)
            val tokens = event.message.tokenize().allTokens().map { Token(event.client, it.content) }
            args["tokenCount"] = tokens.size
            val maxIndex = items.map { it.first + it.second.maxLength }.max() ?: 0
            val minIndex = items.map { it.first + it.second.minLength }.max() ?: 0

            if (tokens.size > maxIndex || tokens.size < minIndex) return@IListener

            launch {
                val ranges = items.map { it.toRange(tokens.size) }
                if(ranges.all {
                            it.second.apply(
                                    tokens.subList(it.first.first, it.first.second), event, services, args)
                        })
                {
                    val context = CommandContext(
                            services = services,
                            args = args,
                            event = event
                    )

                    services.context = context
                    command.handler!!.invoke(context)
                }
            }
        }
    }

}

private data class CharCursor(val content: String, internal val counter : AtomicInteger = AtomicInteger()){

    val isEmpty get() = counter.get() >= content.length
    val hasNext inline get() = !isEmpty

    val char get() = content[counter.get()]
    fun next() { counter.incrementAndGet() }
    operator fun get(pos : Int) = content[pos]

}

private data class NormalizingContext(
        val tokenSubstitutes : Map<Char, Argument>,
        val argumentSubstitutes : Map<String, Argument>
)
