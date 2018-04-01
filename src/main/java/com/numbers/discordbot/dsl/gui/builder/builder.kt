package com.numbers.discordbot.dsl.gui.builder

import com.numbers.discordbot.dsl.gui.extensions.observable
import com.numbers.discordbot.dsl.gui2.*
import com.numbers.discordbot.service.WikiSearchResult
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


object Timer {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun of(duration: Duration, block: () -> Boolean): Observable {
        return object : Observable {
            private val listeners = mutableListOf<InvalidationListener>()
            private var scheduler: ScheduledExecutorService? = null

            override fun removeListener(listener: InvalidationListener) {
                listeners.remove(listener)
                if (listeners.isEmpty()) {
                    scheduler?.shutdown()
                    scheduler = null
                }
            }

            override fun addListener(listener: InvalidationListener) {
                listeners.add(listener)
                if (scheduler == null) {
                    scheduler = Executors.newSingleThreadScheduledExecutor { Thread(it, "OBSERVABLE-TIMER-THREAD") }
                    scheduler?.scheduleAtFixedRate({
                        logger.debug("sending tick, next in {} seconds", duration.seconds)
                        try {
                            if (block()) {
                                listeners.parallelStream().forEach { it.invalidated(this) }
                            }
                        } catch (ex: Exception) {
                            logger.error("exception in screen timer: {}", ex)
                        }
                    }, duration.seconds, duration.seconds, TimeUnit.SECONDS)
                }
            }
        }
    }

    infix fun of(duration: Duration): Observable {
        return object : Observable {
            private val listeners = mutableListOf<InvalidationListener>()
            private var scheduler: ScheduledExecutorService? = null

            override fun removeListener(listener: InvalidationListener) {
                listeners.remove(listener)
                if (listeners.isEmpty()) {
                    scheduler?.shutdown()
                    scheduler = null
                }
            }

            override fun addListener(listener: InvalidationListener) {
                listeners.add(listener)
                if (scheduler == null) {
                    scheduler = Executors.newSingleThreadScheduledExecutor { Thread(it, "OBSERVABLE-TIMER-THREAD") }
                    scheduler?.scheduleAtFixedRate({
                        logger.debug("sending tick, next in {} seconds", duration.seconds)
                        try {
                            listeners.parallelStream().forEach { it.invalidated(this) }
                        } catch (ex: Exception) {
                            logger.error("exception in screen timer: {}", ex)
                        }
                    }, duration.seconds, duration.seconds, TimeUnit.SECONDS)
                }
            }
        }
    }
}

fun WikiSearchResult.toSelectList(): ScreenBuilder.() -> Unit = {
    property(deletable)

    list(items.observable) {
        properties(Controlled, NavigationType.pageNavigation)

        render {
            embedFieldBuilder {
                title = it.title
                description = it.description
            }
        }
    }


}

val Int.seconds: Duration get() = Duration.ofSeconds(this.toLong())

class Emote {
    companion object {
        const val eject = "⏏"
        const val close = "❌"
        const val fastForward = "⏩"
        const val lowVolume = "\uD83D\uDD08"
        const val highVolume = "\uD83D\uDD0A"
        const val stop = "\u23F9"
        const val pausePlay = "⏯"
        const val next = "▶"
        const val prev = "◀"
        const val zero = "\u0030\u20E3"
        const val one = "\u0031\u20E3"
        const val two = "\u0032\u20E3"
        const val three = "\u0033\u20E3"
        const val four = "\u0034\u20E3"
        const val five = "\u0035\u20E3"
        const val six = "\u0036\u20E3"
        const val seven = "\u0037\u20E3"
        const val eight = "\u0038\u20E3"
        const val nine = "\u0039\u20E3"
    }
}