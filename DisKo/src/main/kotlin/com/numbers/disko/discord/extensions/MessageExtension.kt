package com.numbers.disko.discord.extensions

import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.RequestBuffer
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MessageUtils {

    companion object {
        val defaultService = Executors.newSingleThreadScheduledExecutor { Thread(it, "AUTO-DELETE SCHEDULER") }!!
    }
}

fun IMessage.autoDelete(after: Long = 10, time: TimeUnit = TimeUnit.SECONDS, service: ScheduledExecutorService = MessageUtils.defaultService) {
    service.schedule({ RequestBuffer.request { this.delete() } }, after, time)
}
