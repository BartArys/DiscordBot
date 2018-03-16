package com.numbers.discordbot

import com.numbers.discordbot.dsl.gui.builder.Emote
import com.numbers.discordbot.dsl.gui.builder.Timer
import com.numbers.discordbot.dsl.gui.builder.seconds
import com.numbers.discordbot.dsl.gui.extensions.skip
import com.numbers.discordbot.dsl.gui2.*
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.module.music.format

fun MusicPlayer.toScreen() : ScreenBuilder.() -> Unit = {

    onRefresh {
        title = "currently ${if(this@toScreen.isPaused) "paused" else "playing ${this@toScreen.currentTrack?.identifier ?: "nothing \uD83D\uDD07"}" }"
        description = currentTrack?.format(true)
    }

    list(scheduler.tracksProperty.skip(1)){
        properties(Controlled, NavigationType.roundRobinNavigation)

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
    }

    field(volumeProperty){
        title = "Volume"
        description = volume.toString()
    }

    field(Timer of 3.seconds){
        title = "forced refresh"
        description = "every 5 seconds"
    }

}