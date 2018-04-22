package com.numbers.discordbot.dsl

import com.numbers.discordbot.dsl.command.FixedLengthCommand
import com.numbers.discordbot.dsl.command.LenientCommand
import com.numbers.discordbot.dsl.command.PlainTextCommand
import com.numbers.discordbot.dsl.permission.PermissionSupplier
import org.slf4j.LoggerFactory
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class CommandCompiler(format: String, context: ArgumentContext, val command: Command, val services: Services, val supplier: PermissionSupplier) {

    private val context: NormalizingContext = context.normalized()
    private val normalizedFormat: String = format.normalized()

    private val special: CharArray = charArrayOf('{', '|', '}', '?')
    private val tokens get() = context.tokenSubstitutes.keys.toCharArray()

    private fun ArgumentContext.normalized(): NormalizingContext {
        val argumentSubs: Map<String, Argument> = tokenSubstitutes.map { it.toString() to it.value }
                .map { it.second.toKeyedArguments().toMutableMap() }
                .reduce { acc, map -> (acc + map).toMutableMap() } + argumentSubstitutes

        return NormalizingContext(
                tokenSubstitutes = tokenSubstitutes,
                argumentSubstitutes = argumentSubs
        )
    }

    private fun String.normalized(): String {
        val cursor = CharCursor(this)
        val builder = StringBuilder()
        while (cursor.hasNext) {
            builder.append(cursor.consumeUntil(*tokens))
            if (cursor.hasNext) {
                val argumentKey = context.tokenSubstitutes[cursor.char]!!.toKeyedArguments().entries.first().key
                builder.append('{').append(argumentKey).append('}')
                cursor.next()
            }

        }
        return builder.toString()
    }

    private fun parse(): List<FilterItem> {
        val cursor = CharCursor(normalizedFormat)
        val queue = LinkedList<FilterItem>()
        cursor.consumeWhile(' ')
        while (cursor.hasNext) {
            when (cursor.char) {
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
                    if (cursor.hasNext && cursor.char !in special) {
                        val suffix = cursor.consumeUntil(*special, ' ')
                        if (suffix.isNotEmpty() && suffix.isNotBlank()) {
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
                    val content = cursor.consumeUntil(*special, ' ')
                    if (cursor.hasNext && cursor.char == '{') {
                        cursor.next()
                        var arg = context.argumentSubstitutes[cursor.consumeUntil(*special)]!!
                        while (cursor.char != '}') {
                            cursor.next()
                            arg = arg or context.argumentSubstitutes[cursor.consumeUntil(*special)]!!
                        }
                        cursor.next()
                        val suffix = cursor.consumeUntil(' ')
                        queue.add(PaddedArgument(arg, content, suffix))
                    } else {
                        queue.add(WordFilterItem(content))
                    }
                }
            }
            cursor.consumeWhile(' ')
        }
        return queue
    }

    private fun CharCursor.findFirst(vararg special: Char): Char? {
        val copy = this.copy(content = content, counter = java.util.concurrent.atomic.AtomicInteger(counter.get()))
        while (copy.hasNext) {
            when (copy.char) {
                in special -> return copy.char
            }
            copy.next()
        }
        return null
    }

    private fun CharCursor.consumeWhile(vararg special: Char): String {
        val builder = StringBuilder()
        while (hasNext && special.contains(char)) {
            builder.append(char)
            next()
        }
        return builder.toString()
    }

    private fun CharCursor.consumeUntil(vararg special: Char): String {
        val builder = StringBuilder()
        while (hasNext && !special.contains(char)) {
            builder.append(char)
            next()
        }
        return builder.toString()
    }

    operator fun invoke(): IListener<MessageReceivedEvent> {
        if (normalizedFormat.none { special.contains(it) }) {
            return PlainTextCommand(command, supplier)
        }

        val items = parse().toTypedArray()
        if (items.none { it.isVararg }) {
            return FixedLengthCommand(items, command, supplier)
        }

        return LenientCommand(items, command, supplier)
    }

    companion object {
        private val logger by lazy {  LoggerFactory.getLogger(CommandCompiler::class.java) }
    }
}

private data class CharCursor(val content: String, internal val counter: AtomicInteger = AtomicInteger()) {

    val isEmpty get() = counter.get() >= content.length
    val hasNext inline get() = !isEmpty

    val char get() = content[counter.get()]
    fun next() {
        counter.incrementAndGet()
    }

    operator fun get(pos: Int) = content[pos]

}

private data class NormalizingContext(
        val tokenSubstitutes: Map<Char, Argument>,
        val argumentSubstitutes: Map<String, Argument>
)
