package com.numbers.discordbot.module.poker

fun Deck.Companion.pokerDeck(): MutableList<PokerCard> = CardValue.values().flatMap { cardValue -> CardType.values().map { cardType -> PokerCard(cardType, cardValue) } }.shuffled().toMutableList()

class PokerCard(type: CardType, value: CardValue, var faceUp: Boolean = false) : Card(type, value) {

    val minValue: Int
        get() {
            return when (value) {
                CardValue.Ace -> 1
                CardValue.Two -> 2
                CardValue.Three -> 3
                CardValue.Four -> 4
                CardValue.Five -> 5
                CardValue.Six -> 6
                CardValue.Seven -> 7
                CardValue.Eight -> 8
                CardValue.Nine -> 9
                CardValue.Ten -> 10
                CardValue.Joker -> 10
                CardValue.Queen -> 10
                CardValue.King -> 10
            }
        }

    val maxValue: Int
        get() {
            return when (value) {
                CardValue.Ace -> 11
                CardValue.Two -> 2
                CardValue.Three -> 3
                CardValue.Four -> 4
                CardValue.Five -> 5
                CardValue.Six -> 6
                CardValue.Seven -> 7
                CardValue.Eight -> 8
                CardValue.Nine -> 9
                CardValue.Ten -> 10
                CardValue.Joker -> 10
                CardValue.Queen -> 10
                CardValue.King -> 10
            }
        }

}

fun PokerCard?.compareTo(card: PokerCard?): Int {
    return if (card == null && this != null) 1
    else if (card != null && this == null) -1
    else if (card == null && this == null) 0
    else {
        val maxComp = this!!.maxValue.compareTo(card!!.maxValue)
        if (maxComp == 0) this.minValue.compareTo(card.minValue) else maxComp
    }

}

class PokerHand : Hand<PokerCard> {
    private val cards: MutableList<PokerCard> = mutableListOf()

    override val worth: Int
        get() {
            val cardsByWorth = cards.sortedByDescending { it.maxValue }
            val maxWorth = cardsByWorth.sumBy { it.maxValue }

            if (maxWorth < 21) return maxWorth

            return cardsByWorth.last().maxValue + cardsByWorth.first().minValue
        }


    override fun addCard(card: PokerCard) {
        if (cards.size > 2) throw IllegalArgumentException("can't have more than 2 cards in a hand")

        cards.add(card)
    }

    fun split(): Hand<PokerCard> {
        with(PokerHand(), {
            addCard(cards.removeAt(1))
            return this@PokerHand
        })
    }

    override fun clear() {
        cards.clear()
    }

}