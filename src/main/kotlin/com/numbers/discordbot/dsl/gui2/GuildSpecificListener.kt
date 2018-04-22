package com.numbers.discordbot.dsl.gui2

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.GuildEvent
import sx.blah.discord.handle.obj.IGuild

class GuildSpecificListener<T> internal constructor(val guild: IGuild, private val subroutine: IListener<T>) : IListener<T> where T : GuildEvent{

    override fun handle(event: T) {
        if(event.guild.longID == guild.longID){
            subroutine.handle(event)
        }
    }

    companion object {
        fun<T> forGuild(guild: IGuild, subroutine: IListener<T>) : IListener<T> where T : GuildEvent{
            return GuildSpecificListener(guild, subroutine)
        }

        inline fun<T> forGuild(guild: IGuild, crossinline subroutine: (T) -> Unit) : IListener<T> where T : GuildEvent{
            return forGuild(guild, subroutine = IListener { subroutine.invoke(it)})
        }
    }
}