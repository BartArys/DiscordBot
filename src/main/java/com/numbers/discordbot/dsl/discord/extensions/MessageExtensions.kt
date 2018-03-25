package com.numbers.discordbot.dsl.discord.extensions

import com.numbers.discordbot.dsl.discord.DiscordMessage
import com.numbers.discordbot.dsl.discord.InternalDiscordMessage
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.RequestBuffer

internal fun<T> (() -> T).executeAsync() : Deferred<T> = async {
    val request = RequestBuffer.IRequest<T> { this@executeAsync() }
    RequestBuffer.request(request).get()
}


internal val IMessage.asDiscordMessage : DiscordMessage get() = InternalDiscordMessage(this)