package com.numbers.discordbot.dsl.gui2

import javafx.beans.Observable
import sx.blah.discord.handle.impl.obj.Embed

open class Field(observables: Iterable<Observable>, val block: () -> Iterable<Embed.EmbedField>)
    : ScreenItem, ObservableScreenItem(observables) {

    override fun render() = block()
}

inline fun ScreenBuilder.field(vararg observables: Observable, crossinline block: EmbedFieldBuilder.() -> Unit) {
    val field = Field(observables.asIterable()) {
        val builder = EmbedFieldBuilder()
        builder.apply(block)
        listOf(builder.build())
    }
    add(field)
}

