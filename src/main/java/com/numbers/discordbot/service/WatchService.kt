package com.numbers.discordbot.service

import com.google.inject.Inject
import com.google.inject.Singleton
import sx.blah.discord.handle.obj.IGuild
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Singleton
class WatchService @Inject constructor(private val scheduledExecutorService: ScheduledExecutorService) : NicknameManager {

    private val guilds : MutableList<IGuild>  = mutableListOf()

    init {
        scheduledExecutorService.scheduleAtFixedRate({ val timeFormat = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

            guilds.forEach {
                val bot = it.client.ourUser
                it.setUserNickname(bot, timeFormat)
            }
        }, 60 - LocalTime.now().second.toLong(), 60, TimeUnit.SECONDS)
    }

    override fun detachFrom(guild: IGuild) {
        guilds.remove(guild)
    }

    override fun attachTo(guild: IGuild) {
        guilds.add(guild)
    }
}