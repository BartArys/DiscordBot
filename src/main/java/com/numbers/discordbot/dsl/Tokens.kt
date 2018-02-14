package com.numbers.discordbot.guard2

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.util.MessageTokenizer

class Token(private val client: IDiscordClient, val content : String){

    @Volatile
    private var tokenizer = MessageTokenizer(client, content)

    private fun reset(){
        tokenizer = MessageTokenizer(client, content)
    }

    val isUserMention: Boolean get() {
        synchronized(this){
            val result = tokenizer.hasNextMention() && tokenizer.nextMention().mentionObject is IUser
            reset()
            return result
        }
    }

    val isVoiceChannel : Boolean get() {
        synchronized(this){
            if(!tokenizer.hasNextMention()) return false

            val token = tokenizer.nextMention().mentionObject
            reset()
            return token is IVoiceChannel
        }
    }

    val isTextChannelMention: Boolean get() {
        synchronized(this){
            if(!tokenizer.hasNextMention()) return false

            val token = tokenizer.nextMention().mentionObject
            reset()
            return token is IChannel && token !is IVoiceChannel
        }
    }


}

val Token.isBoolean : Boolean inline get() = content.toLowerCase() == "true" || content.toLowerCase() == "false"

val Token.isInt : Boolean inline get() = content.toIntOrNull() != null

val Token.isUrl : Boolean inline get() {
    return org.apache.commons.validator.routines.UrlValidator.getInstance().isValid(content)
}