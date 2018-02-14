package com.numbers.discordbot.trigger

import com.google.inject.Inject
import com.numbers.discordbot.service.PersonalityManager
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent

class LeaveTrigger @Inject constructor(private val pm: PersonalityManager) : IListener<UserVoiceChannelLeaveEvent> {
    override fun handle(event: UserVoiceChannelLeaveEvent) {
        launch {
            pm.forUser(event.user).trigger(event)
        }
    }
}

class MessageTrigger @Inject constructor(private val pm: PersonalityManager) : IListener<MessageReceivedEvent> {
    override fun handle(event: MessageReceivedEvent) {
        launch {
            pm.forUser(event.author).trigger(event)
        }
    }
}