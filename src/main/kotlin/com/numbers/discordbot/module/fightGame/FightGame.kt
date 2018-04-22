package com.numbers.discordbot.module.fightGame

interface Entity{
    val mana : PerkableStat
    val health : PerkableStat
    val stamina : PerkableStat

    val strength : PerkableAttribute
    val dexterity : PerkableAttribute
    val intelligence : PerkableAttribute
}

interface Stat{
    val current: Long
    val max : Long

    fun damageBy(amount: Int)
}


interface PerkableStat : Stat{
    val perks : Iterable<Perk>
    val perkTotal : Long

    fun applyPerk(perk: Perk)
}

abstract class AbstractPerkableStat(private val limitCalculator: () -> Int) : PerkableStat{
    override val perks: MutableList<Perk> = mutableListOf()
    override val perkTotal: Long get() = perks.map { it.invoke(current) - current }.sum()

    final override val max: Long get() = limitCalculator() + perkTotal

    override var current: Long = max

    override fun applyPerk(perk: Perk) {
        perks.add(perk)
    }
}

class EntityPerk(limitCalculator: () -> Int) : AbstractPerkableStat(limitCalculator){
    override fun damageBy(amount: Int) {
        current = Math.max(0, current - amount)
    }
}

class PlayerEntity : Entity {
    override val mana: PerkableStat = EntityPerk { 10 + ( intelligence.current *  0.5).toInt() }
    override val health: PerkableStat = EntityPerk { 10 + ( strength.current *  0.5).toInt() }
    override val stamina: PerkableStat = EntityPerk { 10 + ( dexterity.current *  0.5).toInt() }
    override val strength: PerkableAttribute = PerkableAttributeBase(10)
    override val dexterity: PerkableAttribute= PerkableAttributeBase(10)
    override val intelligence: PerkableAttribute = PerkableAttributeBase(10)
}