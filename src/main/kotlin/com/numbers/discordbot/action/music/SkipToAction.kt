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

class SkipToAction {

    @Permissions(Permission.MUSIC)
    @Guards("""
        skips to a track with the given queue number, or containing the given keywords
    """,
            Guard("$ skip to {index}|{name}",
                    Argument(ArgumentType.INT, "the index of the song in the queue"),
                    Argument(ArgumentType.WORDS, "the keywords contained in a track's title")),
            Guard("\$st {index}|{name}",
                    Argument(ArgumentType.INT, "the index of the song in the queue"),
                    Argument(ArgumentType.WORDS, "the keywords contained in a track's title"))
    )
    fun skipTo(event: MessageReceivedEvent, args: CommandArguments, musicPlayer: MusicPlayer, personality: Personality){
        RequestBuffer.request { event.message.delete() }

        args.get<Int>("index")?.let {
            musicPlayer.skip(it)
            RequestBuffer.request { event.channel.sendMessage(personality.skipSongs(it - 1).build()).autoDelete() }
            return
        }

        args.get<String>("name")?.let { name ->
            val index = musicPlayer.scheduler.tracks.indexOfFirst{ it.identifier.toLowerCase().contains(name.toLowerCase()) }
            if(index >= 0) {
                musicPlayer.skip(index)
                RequestBuffer.request { event.channel.sendMessage(personality.skipSongs(index).build()).autoDelete() }
            }
        }
    }

}