package com.numbers.discordbot.commands.defaultCommands

import com.numbers.disko.*
import com.numbers.disko.guard.canSendMessage
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

@CommandsSupplier
fun systemCommands() = commands{

    command("java.version").guard { canSendMessage }.simply {
        respond { description = System.getProperty("java.version") }
    }

    command("system.uptime").guard { canSendMessage }.simply {
        var uptime = ManagementFactory.getRuntimeMXBean().uptime
        val hours = TimeUnit.HOURS.convert(uptime, TimeUnit.MILLISECONDS)
        uptime -= TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS)
        val minutes = TimeUnit.MINUTES.convert(uptime, TimeUnit.MILLISECONDS)
        uptime -= TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES)
        val seconds = TimeUnit.SECONDS.convert(uptime, TimeUnit.MILLISECONDS)

        respond { description = "uptime [$hours:$minutes:$seconds]" }
    }

}