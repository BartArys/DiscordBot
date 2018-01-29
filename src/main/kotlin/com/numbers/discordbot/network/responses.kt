package com.numbers.discordbot.network

data class EightBallResponse(val question: String, val answer: String, val type: String)

data class DiscordEmojiResponse(val id: String, val title: String, val slug: String, val description: String, val category: Long, val submittedBy: String)

fun DiscordEmojiResponse.slugUrl(): String {
    return "https://discordemoji.com/assets/emoji/${this.slug}.png"
}