package com.numbers.discordbot

import com.numbers.discordbot.commands.defaultCommands.musicCommands
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.module.music.MusicPlayerMessageStore
import com.numbers.discordbot.module.music.format
import com.numbers.disko.guard.byUser
import com.numbers.disko.guard.guard
import com.numbers.disko.gui.builder.Emote
import com.numbers.disko.gui.builder.Timer
import com.numbers.disko.gui.builder.seconds
import com.numbers.disko.gui.extensions.skip
import com.numbers.disko.gui2.*
import sx.blah.discord.handle.obj.IUser

fun MusicPlayer.toScreen(author: IUser): ScreenBuilder.() -> Unit = {

    onRefresh {
        title = "currently ${if (this@toScreen.isPaused) "paused" else "playing ${this@toScreen.currentTrack?.identifier
                ?: "nothing \uD83D\uDD07"}"}"
        description = currentTrack?.format(true)
    }

    property(authorDeletable(author))

    list(scheduler.tracksProperty.skip(1), scheduler.currentTrackProperty, pausedProperty) {
        properties(Controlled, NavigationType.roundRobinNavigation)

        controls {
            forEmote(Emote.pausePlay) { _, _ -> this@toScreen.isPaused = !this@toScreen.isPaused }
            forEmote(Emote.stop) { _, _ -> this@toScreen.skipAll() }
            forEmote(Emote.fastForward) { _, _ -> this@toScreen.skip() }
            forEmote(Emote.lowVolume) { _, _ -> volume -= 10 }
            forEmote(Emote.highVolume) { _, _ -> volume += 10 }
        }

        renderIndexed("queue") { index, item ->
            item.format(withIndex = index)
        }
        musicCommands().commands.forEach { addCommand(it) }
    }

    controls {
        forEmote(Emote.close) { screen, event ->
            event.guard({ byUser(author) }) {
                MusicPlayerMessageStore.removeEntity(screen.guild!!.longID)

                screen.message.guild.connectedVoiceChannel?.leave()
            }
        }
    }

    field(volumeProperty, Timer of 5.seconds) {
        title = "Volume"
        description = volume.toString()
    }

}