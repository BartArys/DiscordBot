package com.numbers.discordbot.commands.defaultCommands

import com.numbers.discordbot.extensions.isFull
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.module.music.MusicPlayerMessageStore
import com.numbers.discordbot.toScreen
import com.numbers.disko.CommandsSupplier
import com.numbers.disko.commands
import com.numbers.disko.guard.*
import com.numbers.disko.info
import com.numbers.disko.invoke
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions
import java.awt.Color

@CommandsSupplier
fun voiceCommands() = commands {
    command("£j {u}|{vc}?")
    command("£ join|j {u}|{vc}?") {

        execute {
            val vc = args<IVoiceChannel>("channel")
                    ?: args<IUser>("user")?.getVoiceStateForGuild(guild)?.channel
                    ?: author.getVoiceStateForGuild(guild)?.channel
                    ?: guild!!.voiceChannels.firstOrNull { it.getModifiedPermissions(bot).contains(Permissions.VOICE_CONNECT) }

            if (vc == null) {
                guard({ canSendMessage }) {
                    respond.error.autoDelete {
                        description = "no suitable channels to join"
                    }
                }
                return@execute
            }

            val connected = bot.getVoiceStateForGuild(event.guild)?.channel

            if (connected == vc) {
                respond.error.autoDelete {
                    description = "already in ${connected.name}"
                }
                return@execute
            }

            guard({ canSendMessage }) {
                if (!vc.canJoin) {
                    if (vc.isFull) {
                        respond {
                            color = Color.red
                            description = "can't join due to the server being full"
                        }
                        return@execute
                    } else {
                        respond {
                            color = Color.red
                            description = "can't join channel due to lacking permissions"
                        }
                    }
                    return@execute
                }
            }

            vc.guard({ canJoin and canSpeak }) {
                vc.join()

                guard({ canMessage }) {
                    MusicPlayerMessageStore(guild!!.longID) {
                        respond.screen("building player...", services<MusicPlayer>().toScreen(author)).await()
                    }
                }
            }

            guard({ canDeleteMessage }) { message.delete() }
        }

        info {
            description = "joins a user or voice channel"
            name = "join"
        }
    }

    command("£l")
    command("£ leave") {

        execute {
            guard({ canDeleteMessage }) { message.delete() }

            bot.getVoiceStateForGuild(guild)?.channel?.let {
                guard({ canSendMessage }) {
                    respond.autoDelete {
                        description = "left ${it.name}"
                    }
                }

                MusicPlayerMessageStore(guild!!.longID)?.delete()
                MusicPlayerMessageStore.removeEntity((guild!!.longID))
                it.leave()
            }
        }

        info {
            description = "leaves the voice channel"
            name = "leave"
        }
    }

    command("£g")
    command("£ gather") {

        execute {
            val channel = author.getVoiceStateForGuild(guild)?.channel

            if (channel == null) {
                guard({ canSendMessage }) {
                    respond.error.autoDelete {
                        description = "caller is not in a voice channel"
                    }
                }
                return@execute
            }

            channel.guard({ canMove }) {
                guild!!.users.filter { !it.isBot }
                        .filter { it.getVoiceStateForGuild(guild)?.channel != null }
                        .forEach { it.moveToVoiceChannel(channel) }

                guard({ canDeleteMessage }) { message.delete() }
            }
        }

        info {
            description = "gather all voice users into the caller's channel"
            name = "gather"
        }
    }
}
