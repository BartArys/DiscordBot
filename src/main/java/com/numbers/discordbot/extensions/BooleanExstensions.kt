package com.numbers.discordbot.extensions

inline fun Boolean.andIf(value: Boolean, block: (Boolean) -> Unit) : Boolean {
    if(value == this) block(this)
    return this
}

inline infix fun Boolean.andIfTrue(block: (Boolean) -> Unit) : Boolean = andIf(true, block)
inline infix fun Boolean.andIfFalse(block: (Boolean) -> Unit) : Boolean = andIf(true, block)