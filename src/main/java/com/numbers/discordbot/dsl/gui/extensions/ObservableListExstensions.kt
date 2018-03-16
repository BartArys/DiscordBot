package com.numbers.discordbot.dsl.gui.extensions

import javafx.collections.FXCollections
import javafx.collections.ObservableList

fun<T> ObservableList<T>.skip(amount: Int) : ObservableList<T>{
    val list = FXCollections.observableArrayList(this.drop(amount))
    //TODO: Actually make this efficient
    this.addListener(javafx.collections.ListChangeListener<T> {
        list.setAll(this@skip.drop(amount))
    })
    return list
}

val <T> Iterable<T>.observable : ObservableList<T> get() {
    return FXCollections.observableArrayList(this.toList())
}

val <T> Array<T>.observable : ObservableList<T> get() {
    return FXCollections.observableArrayList(this.toList())
}
