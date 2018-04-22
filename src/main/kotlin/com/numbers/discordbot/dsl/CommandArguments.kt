package com.numbers.discordbot.dsl

import java.util.*

interface CommandArguments {

    val data: MutableMap<String, Any>

    companion object Factory {
        operator fun invoke(data: MutableMap<String, Any> = mutableMapOf()) = object : CommandArguments {
            override val data: MutableMap<String, Any> = data
        }

        val empty: CommandArguments by lazy {
            object : CommandArguments {
                override val data: MutableMap<String, Any> = Collections.emptyMap()

            }
        }
    }

}

inline operator fun <reified T> CommandArguments.get(key: String): T? = data[key].let { it as T }

inline fun <reified T> CommandArguments.listOf(key: String): List<T>? = this.invoke<List<*>>(key)?.map { it as T }

inline operator fun <reified T> CommandArguments.invoke(key: String): T? = this[key]

operator fun CommandArguments.set(key: String, value: Any) {
    data[key] = value
}
