package com.numbers.discordbot.action.music

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RequestBuffer

class SkipAction  {

    @Permissions(Permission.MUSIC)
    @Guards("""
        skips the given amount or all of songs
    """,
            Guard("$ skip|s {amount}|all?", Argument(ArgumentType.INT, "the amount of songs to skip")),
            Guard("\$s {amount}|all?", Argument(ArgumentType.INT, "the amount of songs to skip"))
    )
    fun skip(event: MessageReceivedEvent, args: CommandArguments, personality: Personality, musicPlayer: MusicPlayer){
        val amount = args.get<Int>("amount") ?: if(args.get<Int>("tokenCount")!! == 3) musicPlayer.scheduler.tracks.count() else 1
        musicPlayer.skip(amount)
        RequestBuffer.request { event.message.delete() }
        RequestBuffer.request { event.channel.sendMessage(personality.skipSongs(amount).build()).autoDelete() }
    }

}