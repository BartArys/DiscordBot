package com.numbers.discordbot.commands.defaultCommands

import com.numbers.discordbot.dsl.CommandsSupplier
import com.numbers.discordbot.dsl.commands
import com.numbers.discordbot.dsl.guard.canSendMessage
import com.numbers.discordbot.dsl.gui2.Controlled
import com.numbers.discordbot.dsl.gui2.NavigationType
import com.numbers.discordbot.dsl.gui2.deletable
import com.numbers.discordbot.dsl.gui2.list

@CommandsSupplier
fun versionCommands() = commands{

    command("java.version").guard { canSendMessage }.simply {
        respond { description = System.getProperty("java.version") }
    }

    command("system.properties").guard { canSendMessage }.simply {
        respond.screen {
            property(deletable)

            list(System.getProperties().map { "${it.key}:${it.value}" }){
                properties(NavigationType.roundRobinNavigation, Controlled)

                render("system properties"){ it }
            }
        }
    }
}