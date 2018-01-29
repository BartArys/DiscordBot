package com.numbers.discordbot.action

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.service.TagService
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.RequestBuffer

class TagDeclaimAction {

    @Guard("$ tag declaim {tag}", Argument(ArgumentType.WORD, "the tag to declaim"))
    fun deClaim(channel: IChannel, message: IMessage, args: CommandArguments, service: TagService){
        launch {
            RequestBuffer.request { channel.toggleTypingStatus() }
            val tag = service.get(args["tag"]!!)
            RequestBuffer.request { message.delete() }
            if(tag == "tag has not been assigned yet"){
                RequestBuffer.request { channel.sendMessage(tag).autoDelete() }
            }else{
                service.remove(args["tag"]!!)
                RequestBuffer.request { channel.sendMessage("tag has been deleted").autoDelete() }
            }
        }
    }
}