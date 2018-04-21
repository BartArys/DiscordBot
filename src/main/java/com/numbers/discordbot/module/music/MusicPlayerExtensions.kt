package com.numbers.discordbot.module.music

import com.numbers.discordbot.dsl.Sequence
import com.numbers.discordbot.dsl.guard.canDeleteMessage
import com.numbers.discordbot.dsl.guard.guard
import com.numbers.discordbot.dsl.gui.extensions.observable
import com.numbers.discordbot.dsl.gui2.*
import com.numbers.discordbot.dsl.listOf
import com.numbers.discordbot.dsl.positiveInteger
import com.numbers.discordbot.extensions.add

fun List<Track>.toSelectScreen(): ScreenBuilder.() -> Unit = {
    property(deletable)

    val observableList = this@toSelectScreen.observable

    addCommand("close"){
        execute {
            guard( { canDeleteMessage } ) { message.delete() }
            delete()
        }
    }

    addCommand("all") {
        execute {
            services<MusicPlayer>().add(observableList)
            delete()
        }
    }

    addCommand("{numbers}") {
        arguments(Sequence.of(positiveInteger("number"), "numbers"))

        execute {
            val numbers = args.listOf<Int>("numbers") ?: emptyList()
            val player = services<MusicPlayer>()
            guard( { canDeleteMessage } ) { message.delete() }

            when {
                numbers.max()!! > this@toSelectScreen.size -> respond.error {
                    description = "number not in list"
                    autoDelete = true
                }
                else -> {
                    numbers.forEach {
                        observableList.removeAt(it)
                        player.add(observableList[it])
                    }
                }
            }
        }
    }

    description = "multiple tracks found, select by space separated numbers, 'all' or 'none'"

    list(observableList) {
        properties(Controlled, NavigationType.roundRobinNavigation)

        renderIndexed("results") { index, item ->
            item.format(false, index)
        }
    }
}
