package com.numbers.discordbot.dsl

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.util.MessageTokenizer

interface Token{
    val content : String
    val isUserMention: Boolean
    val isVoiceChannel : Boolean
    val isTextChannelMention: Boolean

    companion object {
        operator fun invoke(client: IDiscordClient, content: String) : Token = TokenImpl(client, content)
    }

}

private class TokenImpl(private val client: IDiscordClient, override val content : String) : Token{

    @Volatile
    private var tokenizer = MessageTokenizer(client, content)

    private fun reset(){
        tokenizer = MessageTokenizer(client, content)
    }

    override val isUserMention: Boolean get() {
        synchronized(this){
            val result = tokenizer.hasNextMention() && tokenizer.nextMention().mentionObject is IUser
            reset()
            return result
        }
    }

    override val isVoiceChannel : Boolean get() {
        synchronized(this){
            if(!tokenizer.hasNextMention()) return false

            val token = tokenizer.nextMention().mentionObject
            reset()
            return token is IVoiceChannel
        }
    }

    override val isTextChannelMention: Boolean get() {
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