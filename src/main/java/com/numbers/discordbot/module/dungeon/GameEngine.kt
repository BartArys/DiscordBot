package com.numbers.discordbot.module.dungeon

interface GameEngine {


    fun interpret(action: String){

    }


    fun save() : String



    companion object {
        fun from(save: String) : GameEngine{
            TODO()
        }


    }

}