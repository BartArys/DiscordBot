@file:Suppress("unused")

package com.numbers.disko

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.util.*
import kotlin.math.min

internal val SingleValueLength = 1..1

interface FilterItem {
    val length: IntRange

    fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean

    fun asOptional(): FilterItem = OptionalFilterItem(this)


    infix fun or(that: FilterItem): FilterItem = OrFilterItem(this, that)
    infix fun or(that: Argument): FilterItem = OrFilterItem(this, that)
}

open class OrFilterItem(private vararg val items: FilterItem) : FilterItem {

    init {
        if (items.isEmpty()) throw IllegalArgumentException("minimum of 1 argument required")
    }

    override val length: IntRange get() =  items.map { it.length.first }.min()!!..items.map { it.length.last }.max()!!

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        val length = tokens.count()

        val filtered = items
                .filter { it.length.first <= length }
                .filter { it.length.last >= length }

        if (filtered.isEmpty()) return false

        val applies = filtered.map {
            it.apply(tokens.toList().subList(0, min(it.length.last, tokens.size)), event, services, args)
        }
        return applies.any { it }
    }

    override fun toString(): String = items.joinToString(" OR ", "[ ", " ]")
}

open class OptionalFilterItem(val item: FilterItem) : FilterItem {
    override val length: IntRange = 0..item.length.last

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        val length = tokens.count()
        if (length != 0 && length in this.length) {
            item.apply(tokens, event, services, args)
        }

        return true
    }

    override fun toString(): String = "[OPTIONAL: $item ]"
}

class WordFilterItem(val word: String) : FilterItem {
    override val length: IntRange = SingleValueLength

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        return tokens.first().content == word
    }

    override fun toString(): String = "[ $word ]"
}

interface Argument : FilterItem {

    fun toKeyedArguments(): Map<String, Argument>

    override fun asOptional(): Argument {
        return OptionalArgument(this)
    }

    override infix fun or(that: Argument): Argument {
        return OrArgument(this, that)
    }
}

fun word(key: String): Argument = words(key, 1)
fun words(key: String, ofAmount: Int): Argument = words(key, ofAmount..ofAmount)
fun words(key: String, range: IntRange = 1..2000): Argument = WordSequenceArgument(key, range)
fun literal(literal: String, key: String = "LITERAL_" + Random().nextInt(Int.MAX_VALUE), ignoreCase: Boolean = true): Argument = object : Argument {
    override val length: IntRange = SingleValueLength

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        return if (ignoreCase) {
            tokens.first().content.toLowerCase() == literal.toLowerCase()
        } else {
            tokens.first().content == literal
        }
    }

    override fun toKeyedArguments(): Map<String, Argument> = mapOf(key to this)

    override fun toString(): String = "[ $literal ]"
}

fun integer(key: String): Argument = range(key, Int.MIN_VALUE..Int.MAX_VALUE)
fun positiveInteger(key: String): Argument = range(key, 0..Int.MAX_VALUE)
fun strictPositiveInteger(key: String): Argument = range(key, 1..Int.MAX_VALUE)
fun negativeInteger(key: String): Argument = range(key, Int.MIN_VALUE..0)
fun strictNegativeInteger(key: String): Argument = range(key, Int.MIN_VALUE..-1)
fun range(key: String, range: IntRange): Argument = IntRangeArgument(key, range)

fun url(key: String): Argument = SingleTokenArgument(key) { token, _, _, args -> token.isUrl.alsoIfTrue { args[key] = token.content } }
fun userMention(key: String): Argument = SingleTokenArgument(key) { token, event, _, args ->
    token.isUserMention.alsoIfTrue {
        args[key] = event.client.getUserByID(token.content.toUserId())
    }
}

fun textChannelMention(key: String): Argument = SingleTokenArgument(key) { token, _, _, args -> token.isTextChannelMention.alsoIfTrue { args[key] = token.content.removePrefix("<#").removeSuffix(">") } }
fun voiceChannel(key: String): Argument = SingleTokenArgument(key) { token, event, _, args ->
    event.client.voiceChannels.firstOrNull { it.name == token.content }?.let {
        args[key] = it
        true
    } ?: false
}

val appMention: Argument = SingleTokenArgument("bot") { token, event, _, _ ->
    if (!token.isUserMention) false
    else (token.content.toUserId() == event.client.ourUser.longID)
}

internal fun String.toUserId(): Long {
    return this.removePrefix("<@").removePrefix("!").removeSuffix(">").toLong()
}

class SingleTokenArgument(private val keyword: String, private val matcher: (token: Token, event: MessageReceivedEvent, services: Services, args: CommandArguments) -> Boolean) : Argument {

    override fun toKeyedArguments(): Map<String, Argument> = mapOf(keyword to this)

    override val length: IntRange = SingleValueLength

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean = matcher(tokens.first(), event, services, args)

    override fun toString(): String = "[ CUSTOM TOKEN ]"

}

class IntRangeArgument(private val keyword: String, private val range: IntRange) : Argument {

    override fun toKeyedArguments(): Map<String, Argument> = mapOf(keyword to this)

    override val length: IntRange = SingleValueLength

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        return (tokens.first().content.toIntOrNull()?.let { it in range } ?: false).alsoIfTrue {
            args[keyword] = tokens.first().content.toInt()
        }
    }

    override fun toString(): String = "[INTRANGE: $range ]"
}

class WordSequenceArgument(private val keyword: String, private var range: IntRange) : Argument {

    override fun toKeyedArguments(): Map<String, Argument> = mapOf(keyword to this)

    override val length: IntRange get() { return range }

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        val count = tokens.count()
        return (count <= range.last && count >= range.first).alsoIfTrue {
            args[keyword] = tokens.joinToString(" ") { it.content }
        }
    }

    override fun toString(): String = "[WORD SEQUENCE: $range ]"
}

class OrArgument(private vararg val arguments: Argument) : Argument, OrFilterItem(*arguments) {
    override fun toKeyedArguments(): Map<String, Argument> = arguments.flatMap { it.toKeyedArguments().entries }.map { it.key to it.value }.toMap()
}

class OptionalArgument(private val argument: Argument) : Argument, OptionalFilterItem(argument) {
    override fun toKeyedArguments(): Map<String, Argument> = argument.toKeyedArguments()
}

class PaddedArgument(private val argument: Argument, private val prefix: String, private val suffix: String) : Argument {

    override fun toKeyedArguments(): Map<String, Argument> = argument.toKeyedArguments()

    override val length: IntRange = argument.length

    override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
        val tokens = tokens.toMutableList()

        if (!tokens.first().content.startsWith(prefix)) return false
        if (!tokens.last().content.endsWith(suffix)) return false

        if (tokens.count() == 1) {
            val content = tokens.first().content.removePrefix(prefix).removeSuffix(suffix)
            if (content.isEmpty()) return false
            val first = Token(event.client, content)
            return argument.apply(listOf(first), event, services, args)
        }

        val firstContent = tokens.first().content.removePrefix(prefix)

        val first = if (firstContent.isEmpty()) {
            tokens.removeAt(0)
            tokens.toList()[0]
        } else {
            Token(event.client, firstContent)
        }
        tokens.removeAt(0)


        val lastContent = tokens.last().content.removeSuffix(suffix)
        if (lastContent.isEmpty()) return false

        val last = if (lastContent.isEmpty()) {
            tokens.removeAt(tokens.size - 1)
            tokens.last()
        } else {
            Token(event.client, lastContent)
        }
        tokens.removeAt(tokens.size - 1)

        tokens.add(0, first)
        tokens.add(last)
        return argument.apply(tokens, event, services, args)
    }

    override fun toString(): String = """[PADDED: "$prefix" + "$argument" + "$suffix" ]"""

}

object Sequence {
    private class SeparatedSequence(val argument: Argument, val withKey: String, val separatedBy: Argument) : Argument {
        override val length: IntRange = 1..2000
        init {
            if (separatedBy.length != SingleValueLength) throw IllegalArgumentException("separatedBy needs to receive exactly one token")
        }

        override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
            val dummy = CommandArguments()
            val void = CommandArguments()
            val argumentTokens = mutableListOf<Token>()
            tokens.forEach { token ->
                if (!separatedBy.apply(listOf(token), event, services, void)) {
                    argumentTokens.add(token)
                } else if (argument.apply(argumentTokens, event, services, dummy)) {
                    dummy.data.forEach { _, u ->
                        if (args<Any>(withKey) == null) {
                            args[withKey] = mutableListOf(u)
                        } else {
                            args<MutableList<Any>>(withKey)!!.add(u)
                        }
                    }
                    dummy.data.clear()
                    argumentTokens.clear()
                } else {
                    return false
                }
            }
            return if (argument.apply(argumentTokens, event, services, dummy)) {
                dummy.data.forEach { _, u ->
                    if (args<Any>(withKey) == null) {
                        args[withKey] = mutableListOf(u)
                    } else {
                        args<MutableList<Any>>(withKey)!!.add(u)
                    }
                }
                dummy.data.clear()
                argumentTokens.clear()
                true
            } else false

        }

        override fun toKeyedArguments(): Map<String, Argument> = mapOf(withKey to this)

        override fun toString(): String = "[SEQUENCE: OF: $argument SEPARATOR: $separatedBy ]"
    }

    fun of(argument: Argument, withKey: String, separatedBy: Argument): Argument {
        return SeparatedSequence(argument, withKey, separatedBy)
    }

    fun of(argument: Argument, withKey: String): Argument {
        return object : Argument {
            override val length: IntRange = 1..2000
            init {
                if (argument.length != SingleValueLength) throw IllegalArgumentException("argument needs to receive exactly one token")
            }

            override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
                val dummy = CommandArguments()
                return tokens.takeWhile {
                    argument.apply(listOf(it), event, services, dummy).alsoIfTrue {
                        dummy.data.forEach { _, u ->
                            if (args<Any>(withKey) == null) {
                                args[withKey] = mutableListOf(u)
                            } else {
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

val FilterItem.isVararg: Boolean
    get() {
        return  length != SingleValueLength
    }

val FilterItem.isOptional: Boolean
    get() {
        return length.first == 0
    }
