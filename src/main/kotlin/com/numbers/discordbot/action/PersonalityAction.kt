package com.numbers.discordbot.action

import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.service.PersonalityManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

class PersonalityAction {

    @Guards("""
        Change the way the bot interacts.

        Currently horribly broken.
    """,
            Guard("$ to persona|personality {personality}", Argument(ArgumentType.WORDS, "the name of the personality")),
            Guard("$ tp {personality}", Argument(ArgumentType.WORDS, "the name of the personality"))
    )
    fun to(event: MessageReceivedEvent, args: Map<String,String>, personalityManager: PersonalityManager){
        when (args["personality"]) {
            "deus" -> {
                personalityManager.setForUser(event.author, PersonalityManager.Companion.Personalities.DEUS)
            }
            "astolfo" -> {
                personalityManager.setForUser(event.author, PersonalityManager.Companion.Personalities.ASTOLFO)
            }
            "normal" -> {
                personalityManager.setForUser(event.author, PersonalityManager.Companion.Personalities.DEFAULT)
            }
        }

        event.message.delete()
    }
}