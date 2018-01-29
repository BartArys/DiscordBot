package com.numbers.discordbot.action

import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.service.NickNameService
import com.numbers.discordbot.service.WatchService
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RequestBuffer

class NicknameAction {

    @Guards("""
        change the nickname service.

        Currently supports: watch - become a clock that has molecular precision
    """,
            Guard("$ nick|nickname|nn {service}", Argument(ArgumentType.WORDS, "the name of the service")),
            Guard("\$nn {service}", Argument(ArgumentType.WORDS, "the name of the service"))
    )
    fun nick(event: MessageReceivedEvent, args: CommandArguments, nickNameService: NickNameService, watchService: WatchService){
        when(args.get<String>("service")!!){
            "watch" -> nickNameService.setForGuild(watchService, event.guild)
        }

        RequestBuffer.request {
            event.message.delete()
        }
    }

}