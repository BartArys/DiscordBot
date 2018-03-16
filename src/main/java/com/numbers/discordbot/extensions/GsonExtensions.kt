package com.numbers.discordbot.extensions

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

inline fun<reified T> typeOf(): Type {
    return object : TypeToken<T>() {}.type
}

inline fun<reified T> GsonBuilder.registerTypeAdapter(typeAdapter: Any): GsonBuilder = registerTypeAdapter(typeOf<T>(), typeAdapter)