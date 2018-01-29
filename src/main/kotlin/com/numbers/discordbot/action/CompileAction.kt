package com.numbers.discordbot.action

import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.service.JshellService
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

class CompileAction {

    @Guards("""
        runs the supplied code
        """",
            Guard("```java{code}```", Argument(ArgumentType.WORDS, "the code to run")))
    fun compile(event: MessageReceivedEvent, args: CommandArguments, jshellService: JshellService){
        event.channel.typingStatus = true
        val code = args.get<String>("code")!!.removePrefix("```").removeSuffix("```").removePrefix("java").removePrefix("Java")

        val result = jshellService.execute(code)

        event.channel.sendMessage("```\n$result\n```")
    }

}