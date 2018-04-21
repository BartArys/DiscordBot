package com.numbers.discordbot.dsl

import com.numbers.discordbot.dsl.guard.guard
import com.numbers.discordbot.dsl.permission.Permission
import sx.blah.discord.handle.obj.IGuild


interface Command {
    val usage: String
    var arguments: List<Argument>
    var handler: (suspend (CommandContext) -> Unit)?
    var info: CommandInfo
    var permissions: List<Class<out Permission>>

    fun arguments(vararg arguments: Argument) {
        this.arguments = arguments.toList()
    }

    fun execute(handler: suspend CommandContext.() -> Unit) {
        this.handler =  handler
    }

    fun permissions(vararg permissions: Permission) {
        this.permissions = permissions.toList().map { it::class.java }
    }

}

inline fun Command.execute(crossinline guard: CommandContext.() -> Boolean, noinline handler: CommandContext.() -> Unit) {
    this.handler = { it.guard({ guard(it) }) { handler(it) } }
}

inline fun Command.info(info: CommandInfo.() -> Unit) {
    this.info.info()
}

data class UniversalCommand(
        override val usage: String,
        override var arguments: List<Argument> = emptyList(),
        override var handler: (suspend (CommandContext) -> Unit)? = null,
        override var info: CommandInfo = CommandInfo(),
        override var permissions: List<Class<out Permission>> = emptyList()
) : Command

data class GuildCommand(
        override val usage: String,
        override var arguments: List<Argument> = emptyList(),
        override var handler: (suspend (CommandContext) -> Unit)? = null,
        override var info: CommandInfo = CommandInfo(),
        override var permissions: List<Class<out Permission>> = emptyList(),
        var guild: IGuild
) : Command{

    inline fun Command.execute(crossinline guard: CommandContext.() -> Boolean, noinline handler: CommandContext.() -> Unit) {
        this.handler = { it.guard({ guard(it) && it.guild == this.guild }) { handler(it) } }
    }

    inline fun Command.execute(crossinline handler: suspend CommandContext.() -> Unit) {
        this.handler = { it.guard({ it.guild == this.guild }) { handler(it) } }
    }

}
