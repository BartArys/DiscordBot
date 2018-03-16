package com.numbers.discordbot.dsl.gui.extensions

import javafx.beans.InvalidationListener
import javafx.beans.Observable

fun Iterable<Observable>.combine() : Observable{
    return object : Observable, InvalidationListener{
        private val listeners = mutableListOf<InvalidationListener>()

        override fun invalidated(observable: Observable) {
            listeners.forEach { it.invalidated(this) }
        }

        override fun removeListener(listener: InvalidationListener) {
            listeners.remove(listener)
        }

        override fun addListener(listener: InvalidationListener) {
            listeners.add(listener)
        }

    }.also { combined ->
        this.forEach { it.addListener(combined) }
    }
}