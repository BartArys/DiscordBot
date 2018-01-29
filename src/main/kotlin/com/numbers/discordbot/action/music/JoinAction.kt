package com.numbers.discordbot.action.music

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.sendMessageAsync
import com.numbers.discordbot.extensions.then
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.message.MusicPlayerMessage
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.DisplayMessageService
import com.numbers.discordbot.service.Permission
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.MessageBuilder
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.RequestBuilder

class JoinAction{

    companion object {
        private fun getChannel(message: MessageEvent, preferredPersonId: Long? = null, preferredChannelName: String? = null): IVoiceChannel? {
            message.guild.voiceChannels.find { it.connectedUsers.map { it.longID }.any { it == preferredPersonId } }?.let { return it }
            message.guild.voiceChannels.find { it.name.equals(preferredChannelName, true) }?.let { return it }
            message.guild.voiceChannels.find { it.connectedUsers.map { it.stringID }.any { it == message.author.stringID } }?.let { return it }
            return message.guild.voiceChannels.firstOrNull()
        }
    }

    @com.numbers.discordbot.permission.Permissions(Permission.MUSIC)
    @Guards("""
        Joins the given user or voice channel or the first voice channel in guild.

        Raises error when voice channel could not be found, user is not in a voice channel or bot lacks permission to join.
    """,
            Guard("$ join|j {person}|{channel name}?",
                    Argument(ArgumentType.USER_MENTION, "name of the user to join"),
                    Argument(ArgumentType.WORDS, "the name of the voice channel to join")
            ),
            Guard("\$j {person}|{channel name}?",
                    Argument(ArgumentType.USER_MENTION, "name of the user to join"),
                    Argument(ArgumentType.WORDS, "the name of the voice channel to join")
            )
    )
    fun handle(event: MessageReceivedEvent, args: CommandArguments, personality: Personality, player: MusicPlayer, service : DisplayMessageService) {
        val channel = getChannel(event, args["person"], args["channel name"])
        MessageBuilder(event.client)
        launch {
                when {
                channel == null || !channel.getModifiedPermissions(event.client.ourUser).contains(Permissions.VOICE_CONNECT) ->
                    event.channel.sendMessageAsync(personality.failedToJoinChannel().build()).autoDelete().also { RequestBuffer.request { event.message.delete() } }
                channel.stringID == event.message.guild.connectedVoiceChannel?.stringID ->
                    event.channel.sendMessageAsync(personality.alreadyInChannel(channel).build()).autoDelete().also { event.message.autoDelete() }
                else -> {

                    if(service.messages.any { it.message.guild.stringID == event.message.guild.stringID }){
                        RequestBuffer.request { channel.join() }
                        RequestBuffer.request { event.channel.sendMessage(personality.voiceChannelJoin(channel).build()).autoDelete() }
                    }else{
                            var message: MusicPlayerMessage? = null

                        RequestBuilder(event.client).shouldBufferRequests(true).doAction {
                            val sendMessage = event.channel.sendMessage(personality.voiceChannelJoin(channel).build())
                            message = MusicPlayerMessage(player, sendMessage, { service.messages.removeIf { it.message.stringID == sendMessage.stringID } })
                            service.messages += message!!
                            true
                        }.then {
                            event.message.delete()
                        }.then {
                            channel.join()
                        }.then {
                            message!!.init()
                        }.build()
                    }
                }
            }
        }
    }
}