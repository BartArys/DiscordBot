@file:Suppress("unused")

package com.numbers.disko

data class ArgumentBuilder(val arguments: MutableList<Argument> = mutableListOf(), val builder: StringBuilder = StringBuilder()) {
    infix operator fun plus(argument: Argument): ArgumentBuilder {
        arguments.add(argument)
        builder.append("{${argument.toKeyedArguments().keys.first()}} ")
        return this
    }

    infix operator fun plus(string: String): ArgumentBuilder {
        builder.append(" $string".trim())
        return this
    }

}

fun CommandsContainer.simpleCommand(usage: ArgumentBuilder, create: suspend CommandContext.() -> Unit): Command {
    return command(usage) {
        execute {
            this.create()
        }
    }
}

fun CommandsContainer.simpleCommand(usage: Argument, create: suspend CommandContext.() -> Unit): Command {
    return command(ArgumentBuilder() + usage) {
        execute {
            this.create()
        }
    }
}

fun CommandsContainer.command(usage: ArgumentBuilder, create: (Command.() -> Unit)? = null): Command {
    val command = UniversalCommand  (usage.builder.toString())
    if (create != null) {
        command.create()
        command.arguments(*usage.arguments.toTypedArray())
        commands.add(command)
        commands.filter { it.handler == null }.map {
            it.handler = command.handler
            it.info = command.info
            it.arguments = command.arguments
            it.permissions = command.permissions
        }
    } else {
        commands.add(command)
    }

    return command
}

fun CommandsContainer.command(usage: Argument, create: (Command.() -> Unit)? = null): Command {
    return command(ArgumentBuilder() + usage, create)
}

infix operator fun Argument.plus(literal: String): ArgumentBuilder {
    return ArgumentBuilder(mutableListOf(), StringBuilder()) + this + literal
}

infix operator fun Argument.plus(argument: Argument): ArgumentBuilder {
    return ArgumentBuilder(mutableListOf(), StringBuilder()) + this + argument
}

infix operator fun String.plus(argument: Argument): ArgumentBuilder {
    return ArgumentBuilder(mutableListOf(), StringBuilder()) + this + argument
}

