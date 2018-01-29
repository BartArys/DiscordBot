package com.numbers.discordbot.action

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.sendMessageAsync
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.ClapPermissionService
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

class PleaseClapAction{

    @Guards(""""
        Useful for times where your audience isn't impressed by your speech.
    """,
            Guard("please clap"),
            Guard("jeb"),
            Guard("\$pc")
    )
    fun clap(event: MessageReceivedEvent, personality: Personality, clapPermissionService: ClapPermissionService){
        launch {
            if (clapPermissionService.mayClap(event.author)) {
                event.message.autoDelete(20)
                event.channel.sendMessageAsync(personality.clap().build()).autoDelete(20)
            } else {
                event.message.autoDelete(60)
                event.channel.sendMessageAsync(personality.clapDenied().build()).autoDelete(60)
            }
        }
    }
}