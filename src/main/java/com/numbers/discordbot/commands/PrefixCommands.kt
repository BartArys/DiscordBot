package com.numbers.discordbot.commands

import com.numbers.discordbot.dsl.CommandsSupplier
import com.numbers.discordbot.dsl.commands
import com.numbers.discordbot.dsl.invoke
import com.numbers.discordbot.dsl.word
import com.numbers.discordbot.service.discordservices.PrefixService

@CommandsSupplier
fun prefixCommands() = commands {

    command("restart prefixService")
    simpleCommand("restart prefix service"){
        services<PrefixService>().reconnect()
        respond(":ok_hand:", true)
    }

    command("{bot} set prefix {prefix}")
    command("set prefix {prefix}"){
        arguments(word("prefix"))

        execute {
            services<PrefixService>().setPrefix(author, args("prefix")!!)
            respond {
                description = "prefix set to ${args<String>("prefix")}"
            }
        }
    }

    command("what's my prefix")
    command("{bot} get prefix")
    command("get prefix"){
        arguments(word("prefix"))

        execute {
            respond {
                description = services<PrefixService>().getPrefix(author)
            }
        }
    }

    simpleCommand("get prefix {u}"){
        respond {
            description = services<PrefixService>().getPrefix(args("user")!!)
        }
    }

}