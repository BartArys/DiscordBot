package com.numbers.discordbot.commands

import com.numbers.discordbot.Command
import com.numbers.discordbot.chrono.ReminderSystem
import com.numbers.discordbot.commands.util.skipNextWord
import com.numbers.discordbot.filter.MessageFilter
import com.numbers.discordbot.network.reminder.ReminderResponse
import com.numbers.discordbot.persistence.ReminderRepository
import com.numbers.discordbot.persistence.entities.Reminder
import com.numbers.jttp.Jttp
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Command(name = "Reminder")
class ReminderCommand {

    @Command
    @MessageFilter(eventType = MessageEvent::class, prefixCheck = true, regex = "remind.*", readableUsage = "just ask it to remind someone of something sometime")
    fun reminderPrefix(event: MessageEvent, jttp: Jttp, repository: ReminderRepository, service: ScheduledExecutorService){
        if(event::class.java == MentionEvent::class.java) return

        val now = LocalDateTime.now()

        val response = jttp.get("https://api.wit.ai/message")
                .queryString("v","15102017")
                .queryString("q", event.message.tokenize().skipNextWord().remainingContent.replace("\\s","%20"))
                .header("Authorization", "Bearer CUAXPH57ZJDNY33LICSFG7PUELIP2Y75")
                .asObject(ReminderResponse::class.java)
                .join()
                .response


        val contactName = response.entities.contact.first().value

        if(contactName != "me"){
            event.channel.sendMessage("You can't tell other people to remind them of something, that's not how this works")
            return
        }

        val contactPerson = event.author
        val date = LocalDateTime.parse(response.entities.datetime.first().value, DateTimeFormatter.ISO_DATE_TIME)
        val message = response.entities.reminder.first().value

        event.channel.sendMessage("i will remind ${contactPerson.mention(true)} at ${date.format(DateTimeFormatter.ISO_DATE_TIME)} of \"$message\"")
        val reminder = Reminder()
        reminder.message = message
        reminder.putDateTime(date)
        reminder.isHandled = false
        reminder.userId = contactPerson.longID
        repository.addReminder(reminder)
        service.schedule({ ReminderSystem.deliverReminder(reminder, event.client) }, ChronoUnit.SECONDS.between(now, date), TimeUnit.SECONDS)

    }

}