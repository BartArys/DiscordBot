package com.numbers.discordbot.module.dungeon

interface Equipable : Item {

    val occupies: List<Field>

}