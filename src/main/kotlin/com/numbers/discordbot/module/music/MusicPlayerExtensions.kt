package com.numbers.discordbot.module.music

import com.numbers.disko.Sequence
import com.numbers.disko.guard.canDeleteMessage
import com.numbers.disko.guard.guard
import com.numbers.disko.gui.extensions.observable
import com.numbers.disko.gui2.*
import com.numbers.disko.listOf
import com.numbers.disko.positiveInteger
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
