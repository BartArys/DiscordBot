package com.numbers.discordbot.extensions

import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageTokenizer
import sx.blah.discord.util.RequestBuilder
import java.awt.Color
import java.util.*

fun EmbedBuilder.info(message: String? = null): EmbedBuilder {
    this.withColor(Color.LIGHT_GRAY)
    message?.let { withDesc(message) }
    return this
}

fun EmbedBuilder.success(message: String): EmbedBuilder {
    return this.withColor(Color.GREEN).withDesc(message)
}

fun EmbedBuilder.error(message: String, exception: Throwable? = null): EmbedBuilder {
    this.withColor(Color.RED).withDesc(message)
    exception?.let { this.appendField("stacktrace", exception.localizedMessage, true) }
    return this
}

fun EmbedBuilder.error(): EmbedBuilder {
    return this.withColor(Color.RED)
}


fun <T> List<T>.random(): T = this[Random().nextInt(this.size)]

fun IMessage.skipBeforeLast() : MessageTokenizer{
    var count = 0

    var token = this.tokenize()

    while (token.hasNext()) {
        count++
        token.skipNext()
    }

    token = this.tokenize()
    for(i in 0 until count-1){
        token.skipNext()
    }

    return token
}

fun MessageTokenizer.skipNext(amount : Int = 1): MessageTokenizer {
    for(i in 0 until amount) {
        when{
            this.hasNextMention() -> this.nextMention()
            this.hasNextInvite() -> this.nextInvite()
            this.hasNextEmoji() -> this.nextEmoji()
            else -> this.nextWord()
        }
    }

    return this
}

fun RequestBuilder.then(action: () -> Unit) : RequestBuilder{
    return andThen {
        action()
        true
    }
}

inline fun <reified T>  MessageTokenizer.tokenAt(amount : Int = 1): T? where T : MessageTokenizer.Token{
    for(i in 0 until amount) {
        when{
            this.hasNextMention() -> this.nextMention()
            this.hasNextInvite() -> this.nextInvite()
            this.hasNextEmoji() -> this.nextEmoji()
            this.hasNextInvite() -> this.nextInvite()
            this.hasNextWord() -> this.nextWord()
            else -> return null
        }
    }

    return when{
        this.hasNextMention() && T::class is MessageTokenizer.MentionToken<*> -> this.nextMention() as T
        this.hasNextInvite() && T::class is MessageTokenizer.InviteToken -> this.nextInvite() as T
        this.hasNextEmoji()  && T::class is MessageTokenizer.CustomEmojiToken  ->  this.nextEmoji() as T
        this.hasNextEmoji()  && T::class is MessageTokenizer.UnicodeEmojiToken ->  this.nextEmoji() as T
        this.hasNextInvite() && T::class is MessageTokenizer.InviteToken -> this.nextInvite() as T
        this.hasNextWord()  -> this.nextWord() as T
        else -> return null
    }
}
