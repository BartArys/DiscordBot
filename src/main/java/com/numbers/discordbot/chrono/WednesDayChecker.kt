package com.numbers.discordbot.chrono

import com.google.inject.Inject
import com.numbers.discordbot.commands.util.withDefaultColor
import com.numbers.discordbot.persistence.WednesDayRepository
import com.numbers.discordbot.persistence.entities.WednesDayCheck
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.util.EmbedBuilder
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*

@TimedAction
class WednesDayChecker @Inject constructor(repo: WednesDayRepository, client: IDiscordClient) {init {
        val now = LocalDateTime.now()
        if(now.dayOfWeek == DayOfWeek.WEDNESDAY) {
            var optCheck = repo.wednesDayCheck
            if (!optCheck.isPresent){
                val check = WednesDayCheck()
                check.LastCheckLocalDateTime(now)
                repo.put(check)
                optCheck = Optional.of(check)
            }
            val check = optCheck.get()
            if(check.lastCheckDateTime().dayOfYear != now.dayOfYear){
                check.LastCheckLocalDateTime(now)
                repo.update(check)
                client.channels.forEach { it.sendMessage(EmbedBuilder().withDefaultColor().withImage("http://i1.kym-cdn.com/photos/images/newsfeed/001/091/264/665.jpg").build()) }
            }
        }
    }
}
