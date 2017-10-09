package com.numbers.discordbot.commands

import com.numbers.discordbot.Command
import com.numbers.discordbot.audio.MusicManagerCache
import com.numbers.discordbot.filter.MessageFilter
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.obj.IGuild

@Command
class LeaveVoiceCommand {

    @Command
    @MessageFilter(eventType = MentionEvent::class, mentionsBot = true, regex = ".*leave.*")
    fun handleMention(event: MentionEvent, cache: MusicManagerCache){
        leave(event.guild, cache)
    }

    @Command
    @MessageFilter(eventType = MessageEvent::class, prefixCheck = true, regex = "leave")
    fun handlePrefix(event: MessageEvent, cache: MusicManagerCache){
        leave(event.guild, cache)
    }

    private fun leave(guild : IGuild, cache: MusicManagerCache){
        guild.connectedVoiceChannel?.let {
            it.leave()
            val gmm = cache.getGuildMusicManager(guild)
            gmm.scheduler.clear()
            gmm.player.stopTrack()
        }
    }

}