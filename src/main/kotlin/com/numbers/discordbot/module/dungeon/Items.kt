package com.numbers.discordbot.module.dungeon

interface Item {

    val name: String

    fun apply(to : Being)

}