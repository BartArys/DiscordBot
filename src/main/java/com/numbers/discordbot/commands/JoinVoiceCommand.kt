package com.numbers.discordbot.commands

import com.numbers.discordbot.Command
import com.numbers.discordbot.filter.MessageFilter
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.util.MessageTokenizer

@Command(name = "Join Voice")
class JoinVoiceCommand {

    @Command
    @MessageFilter(mentionsBot = true, regex = ".*join.*", eventType = MentionEvent::class, readableUsage = "join {@<User>||\$channelName}")
    fun handleMention(event: MentionEvent){
        val tokenizer = event.message.tokenize()
        tokenizer.nextMention()
        tokenizer.nextWord()

        join(event, tokenizer)
    }

    @Command
    @MessageFilter(startsWith = "join", prefixCheck = true, eventType = MessageEvent::class, readableUsage = "join {@<User>||\$channelName}")
    fun handlePrefix(event : MessageEvent){
        val tokenizer = event.message.tokenize()
        tokenizer.nextWord()
        tokenizer.nextWord()

        join(event, tokenizer)
    }

    private fun join(event : MessageEvent, tokenizer: MessageTokenizer){
        when {
            tokenizer.hasNextMention() -> joinMention(event, tokenContent = tokenizer.nextWord().content)
            tokenizer.hasNextWord() -> joinChannelName(event, tokenContent = tokenizer.nextWord().content)
            else -> join(event.author, event.guild)
        }
    }

    private fun joinChannelName(event: MessageEvent, tokenContent: String){
        event.guild.getVoiceChannelsByName(tokenContent).first().let { it.join() }
    }

    private fun joinMention(event: MessageEvent, tokenContent: String){
        val mentionId = tokenContent.substring( 3, tokenContent.length - 1).toLong()

        print("id: ${mentionId}")

        event.guild.getUserByID(mentionId).let { join(it, event.guild) }
    }

    private fun join(user: IUser, guild: IGuild){
        val channel : IVoiceChannel =
                guild.voiceChannels.find { it.connectedUsers.contains(user) }
                ?: guild.voiceChannels.first()

        channel.let { join(it, guild) }
    }

    private fun join(channel: IVoiceChannel, guild: IGuild){
        if(guild.connectedVoiceChannel != null && guild.connectedVoiceChannel != channel){
            guild.connectedVoiceChannel.leave()
        }
        channel.join()
    }
}