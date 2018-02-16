package com.numbers.discordbot.dsl

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.util.MessageTokenizer

open class CommandArguments(val client: IDiscordClient) {

    val data = mutableMapOf<String, Any>()

    inline operator fun<reified T> get(key: String) : T?{
        val value = data[key] ?: return null

        if(T::class.isInstance(value)) return value as T

        (value as? MessageTokenizer.MentionToken<*>)?.let {
            if(T::class.isInstance(it.mentionObject)) return it.mentionObject as T
        }

        if(T::class == IVoiceChannel::class) return client.voiceChannels.firstOrNull { it.name == value.toString() } as? T

        return null
    }

    inline fun<reified T> listof(key: String) : List<T>? {
        return this.invoke<List<*>>(key)?.map { it as T }
    }

    inline operator fun<reified T> invoke(key: String) : T?{
        return this[key]
    }

    operator fun set(key: String, value: Any){
        data[key] = value
    }

}