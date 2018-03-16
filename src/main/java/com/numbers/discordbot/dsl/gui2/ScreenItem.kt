package com.numbers.discordbot.dsl.gui2

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import sx.blah.discord.handle.impl.obj.Embed

interface ScreenItem : Observable {
    fun render() : Iterable<Embed.EmbedField>

    fun destroy()
}

abstract class ObservableScreenItem(val observables: Iterable<Observable>) : ScreenItem, InvalidationListener {
    private val listeners : MutableCollection<InvalidationListener> = mutableListOf()

    init {
        observables.forEach { it.addListener(this) }
    }

    override fun removeListener(listener: InvalidationListener?) {
        listeners.remove(listener)
    }

    override fun addListener(listener: InvalidationListener?) {
        listener?.let { listeners.add(listener) }
    }

    override fun destroy() {
        observables.forEach { it.removeListener(this) }
    }

    override fun invalidated(observable: Observable?) {
        listeners.forEach { it.invalidated(this) }
    }

    fun invalidate(){
        listeners.forEach { it.invalidated(this) }
    }

}

data class EmbedFieldBuilder(var title: String? = null, var description: String? = null, var inline : Boolean = false){
    fun build() : Embed.EmbedField{
        return Embed.EmbedField(title!!, description!!, inline)
    }
}

inline fun embedFieldBuilder(block : EmbedFieldBuilder.() -> Unit) : Embed.EmbedField {
    val builder = EmbedFieldBuilder()
    builder.apply(block)
    return builder.build()
}