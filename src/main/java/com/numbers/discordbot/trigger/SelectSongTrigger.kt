package com.numbers.discordbot.trigger

import com.google.inject.Inject
import com.google.inject.Singleton
import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.module.music.MusicManager
import com.numbers.discordbot.service.SongSelectService
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

@Singleton
class SelectSongTrigger @Inject constructor(private val selectService: SongSelectService, private val musicManager: MusicManager) : IListener<MessageReceivedEvent>{

    override fun handle(event: MessageReceivedEvent) {
        selectService.getFor(event.author, event.channel)?.let { pair ->
            val content = event.message.content.trim()
            when (content) {
                "all" -> {
                    selectService.deleteFor(event.author, event.channel)
                    pair.first.autoDelete(0)
                    event.message.delete()
                    pair.second.forEach { musicManager.playerForGuild(event.guild).add(it) }
                }
                "none" -> {
                    pair.first.autoDelete(0)
                    event.message.delete()
                    selectService.deleteFor(event.author, event.channel)
                }
                else -> {
                    val numbers = content.split(" ").map { it.toIntOrNull() }
                    if(!numbers.contains(null)){
                        selectService.deleteFor(event.author, event.channel)
                        numbers.forEach { musicManager.playerForGuild(event.guild).add(pair.second[it!!]) }
                        pair.first.autoDelete(0)
                        event.message.delete()
                    }
                }
            }
        }
    }

}