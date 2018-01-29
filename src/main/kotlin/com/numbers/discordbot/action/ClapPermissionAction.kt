package com.numbers.discordbot.action

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.ClapPermissionService
import com.numbers.discordbot.service.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RequestBuffer

class ClapPermissionAction {

    @Permissions(Permission.CLAP)
    @Guards(
            """
                sets the permission to receive applause from the bot for a certain user
            """,
             Guard("$ clappermission|cp {user} {toggle}",
                    Argument(ArgumentType.USER_MENTION, "the user whose clappermissions have to be changed"),
                    Argument(ArgumentType.BOOLEAN, "the new value, `true` being allowed")),
            Guard("\$cp {user} {toggle}",
                    Argument(ArgumentType.USER_MENTION, "the user whose clappermissions have to be changed"),
                    Argument(ArgumentType.BOOLEAN, "the new value, `true` being allowed"))
    )
    fun setPermission(event: MessageReceivedEvent, args: Map<String, String>, personality: Personality, clapPermissionService : ClapPermissionService) {
        event.message.autoDelete()
        if (event.author != event.client.applicationOwner) {
            RequestBuffer.request { event.channel.sendMessage(personality.lackPermission().build()).autoDelete() }
            return
        }

        val toggle = args["toggle"]!!.toBoolean()
        val user = event.client.getUserByID(args["user"]!!.toLong())

        clapPermissionService.setPermission(user, toggle)

        RequestBuffer.request { event.channel.sendMessage(personality.clapAllowed(user).build()).autoDelete() }
    }
}