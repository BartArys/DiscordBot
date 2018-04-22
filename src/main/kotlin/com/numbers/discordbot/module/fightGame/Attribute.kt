package com.numbers.discordbot.module.fightGame

interface Attribute{
    val current: Long

    fun increase(amount: Long)
}

open class AttributeBase(override var current: Long) : Attribute{
    override fun increase(amount: Long) {
        if(current + amount <= 18) current += amount
    }
}

interface Debuff{
    val name: String
    val description : String

    operator fun invoke(original: Long) : Long
}

interface DebuffableAttribute: Attribute {
    val debuffs : Iterable<Debuff>
    val debuffTotal : Long

    fun applyDebuff(debuff: Debuff)
}

open class DebuffableAttributeBase(current: Long) : DebuffableAttribute, AttributeBase(current){
    override val debuffs: MutableList<Debuff> = mutableListOf()

    override val debuffTotal: Long get() = debuffs.map { it.invoke(current) - current }.sum()

    override fun applyDebuff(debuff: Debuff) {
        debuffs.add(debuff)
    }

}

interface Perk {
    val name: String
    val description : String

    operator fun invoke(original: Long) : Long
}

interface PerkableAttribute : DebuffableAttribute {
    val perks: Iterable<Perk>
    val perkTotal : Long

    fun applyPerk(perk: Perk)
}

open class PerkableAttributeBase(current: Long) : PerkableAttribute, DebuffableAttributeBase(current) {
    override val perks: MutableList<Perk> = mutableListOf()
    override val perkTotal: Long get() = perks.map { it.invoke(current) - current }.sum()

    override fun applyPerk(perk: Perk) {
        perks.add(perk)
    }

}