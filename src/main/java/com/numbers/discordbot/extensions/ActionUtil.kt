package com.numbers.discordbot.extensions

import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuilder
import java.awt.Color
import java.util.*

fun EmbedBuilder.info(message: String? = null): EmbedBuilder {
    this.withColor(Color.LIGHT_GRAY)
    message?.let { withDesc(message) }
    return this
}


fun <T> List<T>.random(): T = this[Random().nextInt(this.size)]

fun RequestBuilder.then(action: () -> Unit) : RequestBuilder{
    return andThen {
        action()
        true
    }
}

fun RequestBuilder.first(action: () -> Unit) : RequestBuilder{
    return doAction {
        action()
        true
    }
}

