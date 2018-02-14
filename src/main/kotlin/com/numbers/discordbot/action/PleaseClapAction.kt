package com.numbers.discordbot.action

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.sendMessageAsync
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.Permission
import com.numbers.discordbot.service.PermissionsService
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

class PleaseClapAction{

    @Permissions(Permission.CLAP)
    @Guards(""""
        Useful for times where your audience isn't impressed by your speech.
    """,
            Guard("please clap"),
            Guard("jeb"),
            Guard("\$pc")
    )
    fun clap(event: MessageReceivedEvent, personality: Personality, permissionService: PermissionsService){
        launch {
                event.message.autoDelete(20)
                event.channel.sendMessageAsync(personality.clap().build()).autoDelete(20)
        }
    }
}