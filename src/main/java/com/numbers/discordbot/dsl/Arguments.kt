package com.numbers.discordbot.dsl

import com.numbers.discordbot.extensions.andIfTrue
import com.numbers.discordbot.service.discordservices.PrefixService
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.util.*
import kotlin.math.min

interface FilterItem{
    val minLength: Int get() = 1
    val maxLength: Int get() { return minLength }

    fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments) : Boolean

    fun asOptional() : FilterItem = OptionalFilterItem(this)


    infix fun or(that:FilterItem) : FilterItem = OrFilterItem(this, that)
    infix fun or(that: Argument) : FilterItem = OrFilterItem(this, that)
}

internal open class OrFilterItem(private vararg val items: FilterItem) : FilterItem{

    init {
        if(items.isEmpty()) throw IllegalArgumentException("minimum of 1 argument required")
    }

    override val minLength: Int
        get() = items.map { it.minLength }.min()!!

    override val maxLength: Int
        get() = items.map { it.maxLength }.max()!!

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        val length = tokens.count()

        val filtered = items
                .filter { it.minLength <= length }
                .filter { it.maxLength >= length }

        if(filtered.isEmpty()) return false

        val applies = filtered.map {
            it.apply(tokens.toList().subList(0, min(it.maxLength , tokens.size)), event, services, args)
        }
        return applies.any { it }
    }
}

internal open class OptionalFilterItem(val item: FilterItem) : FilterItem{
    override val minLength: Int
    get() = 0

    override val maxLength: Int
    get() = item.maxLength

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        val length = tokens.count()
        if(length != 0 && length in minLength..maxLength){
            item.apply(tokens, event, services, args)
        }

        return true
    }
}

internal class WordFilterItem(val word : String) : FilterItem{

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments) : Boolean{
        return tokens.first().content == word
    }

}

interface Argument : FilterItem{

    fun toKeyedArguments() : Map<String,Argument>

    override fun asOptional(): Argument {
        return OptionalArgument(this)
    }

    override infix fun or(that: Argument): Argument {
        return OrArgument(this, that)
    }
}
val prefix : Argument = SingleTokenArgument("prefix"){
    token, event, services, args ->
    val prefix = services< PrefixService>().getPrefix(event.author)
    (prefix == token.content).andIfTrue { args["prefix"] = prefix }
}
fun word(key: String) : Argument = words(key, 1)
fun words(key: String, ofAmount : Int) : Argument = words(key,ofAmount..ofAmount)
fun words(key: String, range: IntRange = 1..2000) : Argument = WordSequenceArgument(key, range)
fun literal(literal: String, key: String = "LITERAL_" + Random().nextInt(Int.MAX_VALUE), ignoreCase: Boolean = true) : Argument = object : Argument{

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        return if(ignoreCase){
            tokens.first().content.toLowerCase() == literal.toLowerCase()
        }else{
            tokens.first().content == literal
        }
    }

    override fun toKeyedArguments(): Map<String, Argument> = mapOf(key to this)
}

fun integer(key: String) : Argument = range(key,Int.MIN_VALUE..Int.MAX_VALUE)
fun positiveInteger(key: String) : Argument = range(key,0..Int.MAX_VALUE)
fun strictPositiveInteger(key: String) : Argument = range(key,1..Int.MAX_VALUE)
fun negativeInteger(key: String) : Argument = range(key,Int.MIN_VALUE..0)
fun strictNegativeInteger(key: String) :  Argument = range(key,Int.MIN_VALUE..-1)
fun range(key: String, range : IntRange) : Argument = IntRangeArgument(key, range)

fun url(key: String) : Argument = SingleTokenArgument(key) { token, _, _, args -> token.isUrl.andIfTrue { args[key] = token.content } }
fun userMention(key: String) : Argument = SingleTokenArgument(key) { token, event, _, args ->
    token.isUserMention.andIfTrue {
        args[key] = event.client.getUserByID(token.content.toUserId())
    }
}

fun textChannelMention(key: String) : Argument = SingleTokenArgument(key) { token, _, _, args -> token.isTextChannelMention.andIfTrue { args[key] = token.content.removePrefix("<#").removeSuffix(">") } }
fun voiceChannel(key: String): Argument = SingleTokenArgument(key) { token, event, _, args ->
    event.client.voiceChannels.firstOrNull { it.name == token.content }?.let {
        args[key] = it
        true
    } ?: false
}

val appMention : Argument = SingleTokenArgument("bot") { token, event , _, _ ->
    if(!token.isUserMention) false
    else (token.content.toUserId() == event.client.ourUser.longID)
}

internal fun String.toUserId() : Long{
    return this.removePrefix("<@").removePrefix("!").removeSuffix(">").toLong()
}

internal class SingleTokenArgument(private val keyword : String, private val matcher: (token: Token, event: MessageReceivedEvent, services: Services, args: CommandArguments) -> Boolean) : Argument{

    override fun toKeyedArguments(): Map<String, Argument> = mapOf(keyword to this)

    override val minLength: Int = 1
    override val maxLength: Int = 1

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean
        = matcher(tokens.first(), event, services, args)

}

internal class IntRangeArgument(private val keyword: String, private val range: IntRange) : Argument{

    override fun toKeyedArguments(): Map<String, Argument>  = mapOf(keyword to this)

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        return (tokens.first().content.toIntOrNull()?.let { it in range } ?: false).andIfTrue {
            args[keyword] = tokens.first().content.toInt()
        }
    }

}

internal class WordSequenceArgument(private val keyword: String, private var range: IntRange) : Argument{

    override fun toKeyedArguments(): Map<String, Argument> = mapOf(keyword to this)

    override val minLength: Int = range.first

    override val maxLength: Int = range.last

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        val count = tokens.count()
        return  (count <= range.last && count >= range.first).andIfTrue {
            args[keyword] = tokens.joinToString(" ") { it.content }
        }
    }
}

internal class OrArgument(private vararg val arguments: Argument) : Argument, OrFilterItem(*arguments){
    override fun toKeyedArguments(): Map<String, Argument> = arguments.flatMap { it.toKeyedArguments().entries }.map { it.key to it.value }.toMap()
}

internal class OptionalArgument(private val argument: Argument) : Argument, OptionalFilterItem(argument){
    override fun toKeyedArguments(): Map<String, Argument>  = argument.toKeyedArguments()
}

internal class PaddedArgument(private val argument: Argument, private val prefix: String, private val suffix : String) : Argument{

    override fun toKeyedArguments(): Map<String, Argument> = argument.toKeyedArguments()

    override val minLength: Int
        get() = argument.minLength

    override val maxLength: Int
        get() = argument.maxLength

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        val tokens = tokens.toMutableList()

        if(! tokens.first().content.startsWith(prefix)) return false
        if(! tokens.last().content.endsWith(suffix)) return false

        if(tokens.count() == 1){
            val content = tokens.first().content.removePrefix(prefix).removeSuffix(suffix)
            if(content.isEmpty()) return false
            val first = Token(event.client, content)
            return argument.apply(listOf(first), event, services, args)
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
        if(lastContent.isEmpty()) return false

        val last = if(lastContent.isEmpty()){
            tokens.removeAt(tokens.size - 1)
            tokens.last()
        }else{
            Token(event.client, lastContent)
        }
        tokens.removeAt(tokens.size - 1)

        tokens.add(0, first)
        tokens.add(last)
        return argument.apply(tokens, event, services, args)
    }

}

object Sequence{
    fun of(argument: Argument, withKey: String) : Argument {
        return object: Argument{
            override val minLength: Int = 1
            override val maxLength: Int = 2000

            init {
                if(argument.minLength != 1 || argument.maxLength != 1) throw IllegalArgumentException("argument needs to receive exactly one token")
            }

            override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
                val dummy = CommandArguments()
                return tokens.takeWhile {
                    argument.apply(listOf(it), event, services, dummy).andIfTrue {
                        dummy.data.forEach { _, u ->
                            if(args<Any>(withKey) == null){
                                args[withKey] = mutableListOf(u)
                            }else{
                                args<MutableList<Any>>(withKey)!!.add(u)
                            }
                        }
                        dummy.data.clear()
                    }
                }.any()
            }
            override fun toKeyedArguments(): Map<String, Argument> = mapOf(withKey to this)
        }
    }
}


