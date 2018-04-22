package com.numbers.discordbot.dsl.gui2

import com.numbers.discordbot.dsl.discord.DiscordMessage
import com.numbers.discordbot.dsl.discord.InternalDiscordMessage
import com.numbers.discordbot.dsl.discord.extensions.executeAsync
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import sx.blah.discord.handle.obj.IChannel

inline fun Screen.split(block: ScreenBuilder.() -> Unit) {
    val builder = ScreenBuilder(this.message.guild)
    builder.apply(block)
    async {
        val base = channel.respond("building...").await()
        builder.build(base)
    }
}

fun Screen.detach() {
    this.unregister()
    this.removeControls()
}


fun IChannel.respond(content: String): Deferred<DiscordMessage> = { InternalDiscordMessage(sendMessage(content)) }.executeAsync()
