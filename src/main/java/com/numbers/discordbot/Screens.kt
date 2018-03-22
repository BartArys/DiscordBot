package com.numbers.discordbot

import com.numbers.discordbot.dsl.gui.builder.Emote
import com.numbers.discordbot.dsl.gui.builder.Timer
import com.numbers.discordbot.dsl.gui.builder.seconds
import com.numbers.discordbot.dsl.gui.extensions.skip
import com.numbers.discordbot.dsl.gui2.*
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.module.music.MusicPlayerMessageStore
import com.numbers.discordbot.module.music.format

fun MusicPlayer.toScreen() : ScreenBuilder.() -> Unit = {

    onRefresh {
        title = "currently ${if(this@toScreen.isPaused) "paused" else "playing ${this@toScreen.currentTrack?.identifier ?: "nothing \uD83D\uDD07"}" }"
        description = currentTrack?.format(true)
    }

    list(scheduler.tracksProperty.skip(1), scheduler.currentTrackProperty, pausedProperty){
        properties(Controlled, NavigationType.roundRobinNavigation)
        autoDelete = true

        controls {
            forEmote(Emote.pausePlay) { this@toScreen.isPaused = !this@toScreen.isPaused }
            forEmote(Emote.stop) { this@toScreen.skipAll() }
            forEmote(Emote.fastForward) { this@toScreen.skip() }
            forEmote(Emote.lowVolume) { volume -= 10 }
            forEmote(Emote.highVolume) { volume += 10 }
        }

        renderIndexed("queue") { index, item ->
            item.format(withIndex = index)
        }

        val screenFuns : ControlsContext<Screen>.() ->  Unit = {
            forEmote(Emote.close) { MusicPlayerMessageStore.removeEntity(it.guild!!.longID) }
        }

        controls(screenFuns)
    }

    field(volumeProperty, Timer of 5.seconds){
        title = "Volume"
        description = volume.toString()
    }

}