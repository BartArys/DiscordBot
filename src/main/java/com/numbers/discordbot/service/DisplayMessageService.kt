package com.numbers.discordbot.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.numbers.discordbot.message.DisplayMessage
import sx.blah.discord.util.RequestBuffer
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Singleton
class DisplayMessageService @Inject constructor(service : ScheduledExecutorService) {

    val messages : MutableList<DisplayMessage> = mutableListOf()

    private var queued : List<RequestBuffer.RequestFuture<Void>> = listOf()

    init {

        service.scheduleAtFixedRate({

            if(messages.any() && RequestBuffer.getIncompleteRequestCount() == 0 && queued.all { it.isDone || it.isCancelled }){
                queued = messages.filter { it.needsRefresh }.map { message -> RequestBuffer.request { message.refresh() } }.toList()
            }

        }, 0, 1000, TimeUnit.MILLISECONDS)
    }
}