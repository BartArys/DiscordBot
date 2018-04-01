package com.numbers.discordbot.extensions

import java.util.concurrent.atomic.AtomicInteger

fun <T, C> C.weightedWindow(size: Int, toWeight: T.() -> Int): List<List<T>> where C : Collection<T> {
    val windows = mutableListOf<Pair<AtomicInteger, MutableList<T>>>()
    windows.add(AtomicInteger() to mutableListOf())

    for (item in this) {
        val pair = windows.last()
        val weight = item.toWeight()
        val currentWeight = pair.first.get()

        if (weight + currentWeight > size) {
            windows.add(AtomicInteger(weight) to mutableListOf(item))
        } else {
            pair.first.addAndGet(weight)
            pair.second.add(item)
        }
    }
    return windows.map { it.second }
}