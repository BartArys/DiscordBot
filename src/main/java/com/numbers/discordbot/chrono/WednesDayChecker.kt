package com.numbers.discordbot.chrono

import com.numbers.discordbot.commands.util.withDefaultColor
import com.numbers.discordbot.persistence.WednesDayRepository
import com.numbers.discordbot.persistence.entities.WednesDayCheck
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.util.EmbedBuilder
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@TimedAction
class WednesDayChecker {

    private val repo : WednesDayRepository

    constructor(repo: WednesDayRepository, service: ScheduledExecutorService, client: IDiscordClient){
        this.repo = repo
        val now = LocalDateTime.now()

        if(now.dayOfWeek != DayOfWeek.WEDNESDAY) return

        var optCheck = repo.wednesDayCheck
        if (!optCheck.isPresent){
            val check = WednesDayCheck()
            check.lastCheck = now
            optCheck = Optional.of(check)
            repo.put(check)
        }

        val check = optCheck.get()

        if(check.lastCheck.dayOfYear != now.dayOfYear){
            check.lastCheck = now
            repo.update(check)
            service.schedule({client.channels.forEach { it.sendMessage(EmbedBuilder().withDefaultColor().withImage("http://i1.kym-cdn.com/photos/images/newsfeed/001/091/264/665.jpg").build()) }}, 1, TimeUnit.DAYS)
        }


    }

}
