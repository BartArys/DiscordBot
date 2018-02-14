package com.numbers.discordbot.commands

import com.numbers.discordbot.dsl.CommandsSupplier
import com.numbers.discordbot.dsl.commands
import com.numbers.discordbot.extensions.canJoin
import com.numbers.discordbot.extensions.first
import com.numbers.discordbot.extensions.isFull
import com.numbers.discordbot.extensions.then
import com.numbers.discordbot.guard2.andIfTrue
import com.numbers.discordbot.message.MusicPlayerMessage
import com.numbers.discordbot.service.DisplayMessageService
import com.numbers.discordbot.service.Permission
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.RequestBuilder
import java.awt.Color

@CommandsSupplier
fun voiceCommands() = commands {
    command("£j {u}|{vc}?")
    command("£ join|j {u}|{vc}?"){

        permissions(Permission.MUSIC)

        execute {
            val vc = args<IVoiceChannel>("channel")
                    ?: args<IUser>("user")?.getVoiceStateForGuild(guild)?.channel
                    ?: author.getVoiceStateForGuild(guild)?.channel
                    ?: guild.voiceChannels.firstOrNull { it.getModifiedPermissions(bot).contains(Permissions.VOICE_CONNECT) }

            if(vc == null){
                respond {
                    color = Color.red
                    description = "no suitable channels to join"
                }
                return@execute
            }

            val connected = bot.getVoiceStateForGuild(event.guild)?.channel

            if(connected == vc){
                respond{
                    color = Color.red
                    description = "already in ${connected.name}"
                }
                return@execute
            }

            if(!vc.canJoin()){

                if(vc.isFull()){
                    respond {
                        color = Color.red
                        description ="can't join due to the server being full"
                    }
                    return@execute
                }

                respond {
                    color = Color.red
                    description = "can't join channel due to lacking permissions"
                }
                return@execute
            }

            vc.join()

            val msg = respond {
                description = "joined ${vc.name}"
            }

            val displayService = services<DisplayMessageService>()
            if(displayService.messages.none { it.message.guild.stringID == guild.stringID }){
                val musicDisplay = MusicPlayerMessage(services(),  msg.message) {
                    displayService.messages.removeIf { it.message.stringID == msg.message.stringID }
                }

                RequestBuilder(client).shouldBufferRequests(true)
                        .first { displayService.messages.add(musicDisplay) }
                        .then { musicDisplay.init() }
                        .build()

            }
            message.delete()
        }

        info {
            description = "joins a user or voice channel"
            name = "join"
        }
    }

    command("£l")
    command("£ leave"){

        execute {
            message.delete()

            bot.getVoiceStateForGuild(guild)?.channel?.let {
                respond {
                    description = "left ${it.name}"
                    autoDelete = true
                }
                services<DisplayMessageService>().messages.removeIf {message ->
                    (message is MusicPlayerMessage && message.message.guild.stringID == guild.stringID).andIfTrue {
                        RequestBuffer.request { message.message.delete() }
                    }
                }
                it.leave()
            }
        }

        info {
            description = "leaves the voice channel"
            name = "leave"
        }
    }

    command("£g")
    command("£ gather"){

        permissions(Permission.ADMIN)

        execute {
            val channel = author.getVoiceStateForGuild(guild)?.channel

            if(channel == null){
                respondError {
                    description = "caller is not in a voice channel"
                    autoDelete = true
                }
                return@execute
            }

            guild.users.filter { !it.isBot }
                    .filter { it.getVoiceStateForGuild(guild)?.channel != null }
                    .forEach { it.moveToVoiceChannel(channel) }
        }

        info {
            description = "gather all voice users into the caller's channel"
            name = "gather"
        }
    }
}
