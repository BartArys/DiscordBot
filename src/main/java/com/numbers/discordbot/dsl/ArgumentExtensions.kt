package com.numbers.discordbot.dsl

data class ArgumentBuilder(val arguments: MutableList<Argument> = mutableListOf(), val builder: StringBuilder = StringBuilder()){
    infix operator fun plus(argument: Argument) : ArgumentBuilder{
        arguments.add(argument)
        builder.append("{${argument.toKeyedArguments().keys.first()}} ")
        return this
    }

    infix operator fun plus(string: String) : ArgumentBuilder{
        builder.append(" $string".trim())
        return this
    }

}

fun CommandsContainer.simpleCommand(usage: ArgumentBuilder, create: suspend CommandContext.() -> Unit) : Command{
    return command(usage){
        execute {
            this.create()
        }
    }
}

fun CommandsContainer.simpleCommand(usage: Argument, create: suspend CommandContext.() -> Unit) : Command{
    return command(ArgumentBuilder() + usage){
        execute {
            this.create()
        }
    }
}

fun CommandsContainer.command(usage: ArgumentBuilder, create: (Command.() -> Unit)? = null) : Command {
    val command = Command(usage.builder.toString())
    if (create != null) {
        command.create()
        command.arguments(*usage.arguments.toTypedArray())
        commands.add(command)
        subCommands.map {
            command.copy(usage = it)
        }.forEach { commands.add(it) }
        subCommands.clear()
    }else{
        subCommands.add(command.usage)
    }

    return command
}

fun CommandsContainer.command(usage: Argument, create: (Command.() -> Unit)? = null) : Command {
    return command(ArgumentBuilder() + usage, create)
}

infix operator fun Argument.plus(literal: String) : ArgumentBuilder{
        return ArgumentBuilder(mutableListOf(), StringBuilder()) + this + literal
}

infix operator fun Argument.plus(argument: Argument) : ArgumentBuilder{
    return ArgumentBuilder(mutableListOf(), StringBuilder()) + this + argument
}

infix operator fun String.plus(argument: Argument) : ArgumentBuilder{
    return ArgumentBuilder(mutableListOf(), StringBuilder()) + this + argument
}

