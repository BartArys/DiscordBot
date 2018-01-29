package com.numbers.discordbot.action

import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.service.TagService
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer

class TagClaimAction{

    @Guards("""
        Claim a given tag with the given content.

        Raises error when the tag is already claimed.
    """,Guard("$ tag claim {tag} {content}", Argument(ArgumentType.WORD, "the tag to claim"), Argument(ArgumentType.WORDS, "the content to pair the tag with")))
    fun claim(channel: IChannel, args: CommandArguments, service: TagService){
        launch {
            val content = service.get(args["tag"]!!)
            if(content == "tag has not been assigned yet"){
                service.set(args["tag"]!!, args["content"]!!)
                RequestBuffer.request { channel.sendMessage(EmbedBuilder().withDescription("tag set to ${args.get<String>("content")}").build()) }
            }else{
                RequestBuffer.request { channel.sendMessage(EmbedBuilder().withDescription("tag already claimed").build()) }
            }
        }
    }
}