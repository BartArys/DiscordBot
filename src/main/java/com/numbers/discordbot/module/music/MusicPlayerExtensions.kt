package com.numbers.discordbot.module.music

import com.numbers.discordbot.dsl.Sequence
import com.numbers.discordbot.dsl.gui2.Controlled
import com.numbers.discordbot.dsl.gui2.NavigationType
import com.numbers.discordbot.dsl.gui2.ScreenBuilder
import com.numbers.discordbot.dsl.gui2.list
import com.numbers.discordbot.dsl.listOf
import com.numbers.discordbot.dsl.positiveInteger
import com.numbers.discordbot.extensions.add

fun List<Track>.toSelectScreen() : ScreenBuilder.() -> Unit = {
    autoDelete = true

    addCommand("all"){
        execute {
            services<MusicPlayer>().add(this@toSelectScreen)
            delete()
        }
    }

    addCommand("{numbers}"){
        arguments(Sequence.of(positiveInteger("number"), "numbers"))

        execute {
            val numbers = args.listOf<Int>("numbers") ?: emptyList()
            val player = services<MusicPlayer>()
            when {
                numbers.isEmpty() -> {
                    this@toSelectScreen.forEach { player.add(it) }
                    delete()
                }
                numbers.max()!! > this@toSelectScreen.size -> respondError {
                    description = "number not in list"
                    autoDelete = true
                }
                else -> {
                    numbers.forEach { player.add(this@toSelectScreen[it]) }
                }
            }
        }
    }

    description = "multiple tracks found, select by space separated numbers, 'all' or 'none'"

    list(this@toSelectScreen){
        properties(Controlled, NavigationType.roundRobinNavigation)

        renderIndexed("results"){ index, item ->
            item.format(false, index)
        }
    }
}
