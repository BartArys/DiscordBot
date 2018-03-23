package com.numbers.discordbot.dsl.gui2

import com.numbers.discordbot.dsl.discord.InternalDiscordMessage
import com.numbers.discordbot.dsl.discord.extensions.executeAsync
import kotlinx.coroutines.experimental.async
import sx.blah.discord.handle.obj.IChannel

fun Screen.split(block: ScreenBuilder.() -> Unit) = async {
    val builder = ScreenBuilder()
    builder.apply(block)
    val base = channel.respond("building...").await()
    builder.build(base)
}

fun Screen.detach(){
    this.unregister()
    this.removeControls()
}

@Suppress("UNUSED_PARAMETER")
private fun IChannel.respond(content: String, autoDelete: Boolean = false)
        =  { InternalDiscordMessage(sendMessage(content)) }.executeAsync()
