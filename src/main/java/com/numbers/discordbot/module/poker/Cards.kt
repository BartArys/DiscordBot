package com.numbers.discordbot.module.poker

enum class CardType {
    Hearts, Clover, Diamonds, Spades
}

enum class CardValue {
    Ace,
    Two,
    Three,
    Four,
    Five,
    Six,
    Seven,
    Eight,
    Nine,
    Ten,
    Joker,
    Queen,
    King;
}

open class Card(val type: CardType, val value: CardValue) {
    override fun toString(): String {
        return "${value.name} of ${type.name}"
    }
}

interface Hand<in T> where T : Card {

    val worth: Int

    fun addCard(card: T)

    fun clear()
}

class Deck private constructor() {

    companion object

}