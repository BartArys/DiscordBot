package com.numbers.discordbot.dsl

import java.time.Duration

internal inline fun <T> measure(timeResultBlock: (Duration) -> Unit, block: () -> T): T {
    val before = Duration.ofNanos(System.nanoTime())
    val result = block()
    val after = Duration.ofNanos(System.nanoTime())
    timeResultBlock(after.minus(before))
    return result
}

internal inline fun <T> measureIf(boolean: Boolean, timeResultBlock: (Duration) -> Unit, block: () -> T): T {
    return if (boolean) {
        val before = Duration.ofNanos(System.nanoTime())
        val result = block()
        val after = Duration.ofNanos(System.nanoTime())
        timeResultBlock(after.minus(before))
        result
    } else {
        block()
    }
}

internal inline fun <T> org.slf4j.Logger.measureIfDebug(action: String, block: () -> T): T = measureIf(isDebugEnabled, { debug("{} took {} ms", action, it.toMillis()) }, block)
