package com.numbers.discordbot.commands.util

import com.numbers.discordbot.filter.MessageFilter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageTokenizer
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

fun EmbedBuilder.withDefaultColor(): EmbedBuilder = this.withColor(31, 194, 194)
fun EmbedBuilder.withDeleteInfo(): EmbedBuilder = this.withFooterText("this message will be deleted in 10 seconds").withTimestamp(LocalDateTime.now())

fun AudioTrack.infoString(): String = "[${CommandUtil.milliesToDisplayTime(this.position)}][${CommandUtil.milliesToDisplayTime(this.duration)}] [${this.info.title}](${this.info.uri})"
fun<T> List<out T>.random():T = this[Random().nextInt(this.size)]

fun MessageFilter.document() : String{
    val eventType = this.eventType.simpleName?.replace("event", "", true)

    val mention = this.mentionsBot.ifElse("@<BotName>", "<Prefix>")
    val usage = this.readableUsage

    return "**$eventType**: $mention | $usage"
}

fun<T> Boolean.ifElse(t1 : T, t2 : T): T = if(this) t1 else t2

fun MessageTokenizer.skipNextMention(): MessageTokenizer{
    if(this.hasNextMention()) this.nextMention()
    return this
}

fun MessageTokenizer.skipNextWord(): MessageTokenizer{
    if(this.hasNextWord()) this.nextWord()
    return this
}

class CommandUtil{

    companion object Tools {

        fun delete(after: Long, time : TimeUnit, service: ScheduledExecutorService, vararg messages: IMessage){
            service.schedule({ messages.forEach(IMessage::delete) }, after, time)
        }

        fun delete(service: ScheduledExecutorService, vararg messages: IMessage){
            delete(10, TimeUnit.SECONDS, service, *messages)
        }

        fun milliesToDisplayTime(ms : Long) : String = LocalTime.MIN.plus(ms, ChronoUnit.MILLIS).withNano(0).format(DateTimeFormatter.ISO_TIME)

    }

}