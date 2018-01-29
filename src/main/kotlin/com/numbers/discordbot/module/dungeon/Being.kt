package com.numbers.discordbot.module.dungeon

import kotlin.math.max
import kotlin.reflect.KProperty

enum class Field {
    LeftHand,
    RightHand,
    LeftLeg,
    RightLeg,
    LeftFoot,
    RightFoot,
    LeftArm,
    RightArm,
    Head,
    Torso
}

abstract class Being(val stats: Stats, health: Int, mana: Int, healthRemaining : Int = health, manaRemaining: Int = mana)  {

    private var _health : Int = health

    private var _mana : Int = mana

    private var _manaRemaining : Int = manaRemaining

    private var _healthRemaining: Int = healthRemaining

    var health: Int
        get() = _health
        set(value) {
            if(value < healthRemaining) healthRemaining = value
            _health = value
        }

    var mana: Int
        get() = _mana
        set(value) {
            if(value < manaRemaining) manaRemaining = value
            _mana = value
        }

    var healthRemaining: Int
        get() = _healthRemaining
        set(value) {
            if(value > health) throw IllegalArgumentException("value can't be greater than total health")
        }

    var manaRemaining: Int
        get() = _manaRemaining
        set(value) {
            if(value > mana) throw IllegalArgumentException("value can't be greater than total mana")
            _manaRemaining = value
        }

    init {
        if(health < healthRemaining) throw IllegalArgumentException("health can't be lower than remaining")
        if(mana < manaRemaining) throw IllegalArgumentException("mama can't be lower than remaining")
    }


    abstract val fields : Iterable<Field>

    abstract val equipped : MutableIterable<Equipable>

}

data class Stats(var strength : Int, var  dexterity: Int, var constitution: Int, var intelligence: Int, var wisdom: Int, var charisma: Int)


abstract class Humanoid(stats: Stats, health: Int, mana: Int, healthRemaining: Int, manaRemaining: Int) : Being(stats, health, mana, healthRemaining, manaRemaining) {

    override val fields = listOf(Field.Head, Field.Torso, Field.LeftArm, Field.LeftHand, Field.RightArm, Field.RightHand, Field.LeftLeg, Field.RightLeg, Field.LeftFoot, Field.RightFoot)

    override val equipped = mutableListOf<Equipable>()

}

class StatDelegate(default : Int = 0) {

    private var ref : Int = default

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return thisRef as? Int ?: 0
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        ref = max(thisRef as? Int ?: 0, 0)
    }

}

class Hero(stats: Stats, health: Int, mana: Int, healthRemaining: Int, manaRemaining: Int): Humanoid(stats, health, mana, healthRemaining, manaRemaining) {


}