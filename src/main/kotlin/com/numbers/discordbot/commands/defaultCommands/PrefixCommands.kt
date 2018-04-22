package com.numbers.discordbot.commands.defaultCommands

import com.numbers.discordbot.service.discordservices.PrefixService
import com.numbers.disko.CommandsSupplier
import com.numbers.disko.commands
import com.numbers.disko.guard.canSendMessage
import com.numbers.disko.guard.guard
import com.numbers.disko.invoke
import com.numbers.disko.word

@CommandsSupplier
fun prefixCommands() = commands {

    command("restart prefixService")
    command("restart prefix service").simply {
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

    command("get prefix {u}").simply {
        guard({ canSendMessage }) { respond { description = services<PrefixService>().getPrefix(args("user")!!) } }
    }

}