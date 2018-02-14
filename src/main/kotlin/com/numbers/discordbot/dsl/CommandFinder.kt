package com.numbers.discordbot.dsl

import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner

annotation class CommandsSupplier

fun findCommands(`package`: String) : List<Command> {
    val suppliers = Reflections(`package`, MethodAnnotationsScanner()).getMethodsAnnotatedWith(CommandsSupplier::class.java)

    return suppliers.mapNotNull { it.invoke(null) }
           .filterIsInstance<CommandsContainer>()
           .flatMap { it.commands }
}

