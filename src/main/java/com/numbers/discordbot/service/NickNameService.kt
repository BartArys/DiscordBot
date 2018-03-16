package com.numbers.discordbot.service

import com.google.inject.Singleton
import sx.blah.discord.handle.obj.IGuild

@Singleton
class NickNameService{

    private val managers : MutableMap<String, NicknameManager> = mutableMapOf()

    fun setForGuild(manager: NicknameManager, guild: IGuild){
        managers[guild.stringID]?.detachFrom(guild)
        managers[guild.stringID] = manager
        manager.attachTo(guild)
    }

    fun removeForGuild(guild: IGuild){
        managers[guild.stringID]?.detachFrom(guild)
    }
}

interface NicknameManager {

    fun detachFrom(guild: IGuild)
    fun attachTo(guild: IGuild)

}