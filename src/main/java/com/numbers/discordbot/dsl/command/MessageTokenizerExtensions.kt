package com.numbers.discordbot.dsl.command

import sx.blah.discord.util.MessageTokenizer
import java.util.regex.Pattern


internal fun MessageTokenizer.nextToken(): MessageTokenizer.Token? {
    return when {
        this.hasNextMention()
                && hasNextToken(MessageTokenizer.ANY_MENTION_PATTERN) -> this.nextMention()
        this.hasNextInvite()
                && hasNextToken(MessageTokenizer.INVITE_PATTERN) -> this.nextInvite()
        this.hasNextEmoji()
                && hasNextToken(MessageTokenizer.CUSTOM_EMOJI_PATTERN) -> this.nextEmoji()
        this.hasNextWord() -> this.nextWord()
        else -> null
    }
}

internal fun MessageTokenizer.hasNextToken(): Boolean {
    return when {
        this.hasNextMention() -> true
        this.hasNextInvite() -> true
        this.hasNextEmoji() -> true
        this.hasNextWord() -> true
        else -> false
    }
}

internal fun MessageTokenizer.hasNextToken(pattern: Pattern): Boolean {
    val matcher = pattern.matcher(remainingContent.trim())
    if (!matcher.find()) return false

    val start = 0
    val matcherStart = matcher.start()

    return matcherStart == start

}

internal fun MessageTokenizer.allTokens(): List<MessageTokenizer.Token> {
    val tokens = mutableListOf<MessageTokenizer.Token>()
    while (hasNextToken()) tokens.add(nextToken()!!)
    return tokens
}