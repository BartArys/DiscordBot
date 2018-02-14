package com.numbers.discordbot.module.poker

import sx.blah.discord.handle.obj.IUser

class PokerModule {

    companion object {
        fun forPerson(user: IUser): PokerModule {
            return PokerModule()
        }
    }

}