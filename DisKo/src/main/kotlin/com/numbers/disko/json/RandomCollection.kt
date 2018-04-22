package com.numbers.disko.json

import java.util.*

class RandomCollection<E> constructor(private val random: Random = Random()) {
    private val map = TreeMap<Double, E>()
    private var total = 0.0

    fun add(weight: Double, result: E): RandomCollection<E> {
        if (weight <= 0) return this
        total += weight
        map[total] = result
        return this
    }

    operator fun next(): E {
        val value = random.nextDouble() * total
        return map.higherEntry(value).value
    }
}