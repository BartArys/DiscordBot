package com.numbers.disko

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

inline fun <reified T> typeOf(): Type {
    return object : TypeToken<T>() {}.type
}

inline fun <reified T> GsonBuilder.registerTypeAdapter(typeAdapter: Any): GsonBuilder = registerTypeAdapter(typeOf<T>(), typeAdapter)

inline fun Boolean.andIf(value: Boolean, block: (Boolean) -> Unit): Boolean {
    if (value == this) block(this)
    return this
}

inline infix fun Boolean.alsoIfTrue(block: (Boolean) -> Unit): Boolean = andIf(true, block)
inline infix fun Boolean.andIfFalse(block: (Boolean) -> Unit): Boolean = andIf(true, block)