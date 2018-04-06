package com.numbers.discordbot.module.fightGame

interface Entity{
    val mana : Mana
    val health : Health
    val Stamina : Stamina

    val strength : Strength
    val dexterity : Dexterity
    val intelligence : Intelligence
}

interface Stat{
    val current: Long
    val max : Long

    fun damageBy(amount: Int)
}

interface Mana : PerkableStat
interface Health : PerkableStat
interface Stamina : PerkableStat

interface PerkableStat : Stat{
    val perks : Iterable<Perk>
    val perkTotal : Long

    fun applyPerk(perk: Perk)
}

abstract class AbstractPerkableStat(val limitCalculator: () -> Int) : PerkableStat{
    override val perks: MutableList<Perk> = mutableListOf()
    override val perkTotal: Long get() = perks.map { it.invoke(current) - current }.sum()

    final override val max: Long get() = limitCalculator() + perkTotal

    override var current: Long = max

    override fun applyPerk(perk: Perk) {
        perks.add(perk)
    }
}

interface Strength : PerkableAttribute
interface Dexterity : PerkableAttribute
interface Intelligence : PerkableAttribute
