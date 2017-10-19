package com.numbers.discordbot.chrono

import com.google.inject.Inject
import com.numbers.discordbot.persistence.ReminderRepository
import com.numbers.discordbot.persistence.entities.Reminder
import sx.blah.discord.api.IDiscordClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ReminderSystem {

    companion object {
        fun deliverReminder(reminder: Reminder, client: IDiscordClient, late : Boolean = false){
            reminder.isHandled = true
            val user = client.getUserByID(reminder.userId)
            val excuse = if(late)  "sorry i'm late, seems like i've overslept :sleeping_accommodation:\nAnyway, " else ""

            user.orCreatePMChannel.sendMessage("${excuse}Here's your reminder for ${reminder.localDateTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))}\n${reminder.message}")
        }
    }

    @Inject
    constructor(service: ScheduledExecutorService, repository: ReminderRepository, client: IDiscordClient){
        val all = repository.reminders
        all.filter { !it.isHandled && it.localDateTime() <= LocalDateTime.now() }.forEach { deliverReminder(it, client, true) }
        repository.deletReminders(all.filter { it.localDateTime() <= LocalDateTime.now() || it.isHandled })
        repository.reminders.forEach { service.schedule({ deliverReminder(it, client) } ,ChronoUnit.SECONDS.between(it.localDateTime(), LocalDateTime.now()), TimeUnit.SECONDS) }
    }

}