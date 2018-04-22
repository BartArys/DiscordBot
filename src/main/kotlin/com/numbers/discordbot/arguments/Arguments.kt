package com.numbers.discordbot.arguments

import com.numbers.discordbot.extensions.alsoIfTrue
import com.numbers.discordbot.service.discordservices.PrefixService
import com.numbers.disko.*

val prefix: Argument = SingleTokenArgument("prefix") { token, event, services, args ->
    val prefix = services<PrefixService>().getPrefix(event.author)
    (prefix == token.content).alsoIfTrue { args["prefix"] = prefix }
}