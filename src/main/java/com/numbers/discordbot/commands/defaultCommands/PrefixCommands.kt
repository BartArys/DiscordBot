package com.numbers.discordbot.commands.defaultCommands

import com.numbers.discordbot.dsl.CommandsSupplier
import com.numbers.discordbot.dsl.commands
import com.numbers.discordbot.dsl.guard.canSendMessage
import com.numbers.discordbot.dsl.guard.guard
import com.numbers.discordbot.dsl.invoke
import com.numbers.discordbot.dsl.word
import com.numbers.discordbot.service.discordservices.PrefixService

@CommandsSupplier
fun prefixCommands() = commands {

    command("restart prefixService")
    simpleCommand("restart prefix service") {
        services<PrefixService>().reconnect()
        guard({ canSendMessage }) { respond(":ok_hand:", true) }
    }

    command("{bot} set prefix {prefix}")
    command("set prefix {prefix}") {
        arguments(word("prefix"))

        execute {
            services<PrefixService>().setPrefix(author, args("prefix")!!)
            guard({ canSendMessage }) { respond { description = "prefix set to ${args<String>("prefix")}" } }
        }
    }

    command("what's my prefix")
    command("{bot} get prefix")
    command("get prefix") {
        arguments(word("prefix"))

        execute {
            guard({ canSendMessage }) { respond { description = services<PrefixService>().getPrefix(author) } }
        }
    }

    simpleCommand("get prefix {u}") {
        guard({ canSendMessage }) { respond { description = services<PrefixService>().getPrefix(args("user")!!) } }
    }

}