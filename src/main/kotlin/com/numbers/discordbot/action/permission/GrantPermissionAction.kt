package com.numbers.discordbot.action.permission

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.error
import com.numbers.discordbot.extensions.success
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.service.Permission
import com.numbers.discordbot.service.PermissionsService
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer

class GrantPermissionAction{

    @Permissions(Permission.MANAGE)
    @Guards("grants a user permissions", Guard("$ grant {user} {permissions}",
            Argument(ArgumentType.USER_MENTION, "the user to grant permissions to"),
            Argument(ArgumentType.WORDS, "the permissions to add to the user")))
    fun grantPermission(event: MessageReceivedEvent, args: CommandArguments, permissionsService: PermissionsService){
        val mention : IUser = args["user"]!!
        val pms = args.get<String>("permissions")!!.split(" ")

        val permissions = try {
            pms.map { Permission.valueOf(it.trim().toUpperCase()) }
        }catch (ex: IllegalArgumentException){
            RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().error().withDesc("can't add unknown permission").build()).autoDelete() }
            return
        }

        launch {
            var userPermissions = permissionsService.get(mention).toMutableList()
            userPermissions.addAll(permissions)
            userPermissions = userPermissions.distinct().toMutableList()

            permissionsService.set(mention, userPermissions)

            RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().success("permissions added").build()).autoDelete() }
        }

    }

}