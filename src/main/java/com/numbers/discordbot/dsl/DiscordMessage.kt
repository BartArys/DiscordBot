package com.numbers.discordbot.dsl

import com.numbers.discordbot.extensions.autoDelete
import com.vdurmont.emoji.Emoji
import kotlinx.coroutines.experimental.async
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IEmoji
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IReaction
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.RequestBuffer
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.suspendCoroutine

class DiscordMessage(val message: IMessage) {

    val attachments = message.attachments!!
    val author = message.author!!
    val channel = message.channel!!
    val channelMentions = message.channelMentions.toList()
    val content = message.content
    val editedTimeStamp : LocalDateTime? get() = message.editedTimestamp.orElseGet { null }
    val embeds = message.embeds.toList()
    val formattedContent = message.formattedContent
    val guild = message.guild
    val longId = message.longID
    val mentions = message.mentions.toList()
    val reactions = message.reactions.toList()
    val roleMentions = message.roleMentions
    val type = message.type
    val webhookLongId = message.webhookLongID
    val isDeleted = message.isDeleted
    val isPinned = message.isPinned
    val isSystemMessage = message.isSystemMessage
    val mentionsEveryone = message.mentionsEveryone()
    val mentionsHere = message.mentionsHere()
    val tokenized get() = message.tokenize()
    val client = message.client
    val shard = message.shard

    fun addReaction(emoji: ReactionEmoji)  = async {
        RequestBuffer.request {
            message.addReaction(emoji)
        }.get()
    }

    fun addReaction(emoji: Emoji) = async {
        RequestBuffer.request {
            message.addReaction(emoji)
        }.get()
    }

    fun addReaction(emoji: IEmoji) = async {
        RequestBuffer.request {
            message.addReaction(emoji)
        }
    }

    fun addReaction(reaction: IReaction) = async {
        RequestBuffer.request {
            message.addReaction(reaction)
        }
    }

    suspend fun edit(content: String, embed: EmbedObject): IMessage = suspendCoroutine {cont ->
        RequestBuffer.request { cont.resume(message.edit(content, embed)) }
    }

    suspend fun getReactionByEmoji(emoji: IEmoji?): IReaction? = suspendCoroutine {cont ->
        RequestBuffer.request { cont.resume(message.getReactionByEmoji(emoji)) }
    }

    suspend fun getReactionByEmoji(emoji: ReactionEmoji?): IReaction = suspendCoroutine {cont ->
        RequestBuffer.request { cont.resume(message.getReactionByEmoji(emoji)) }
    }

    suspend fun getReactionByID(id: Long): IReaction? = suspendCoroutine {cont ->
        RequestBuffer.request { cont.resume(message.getReactionByID(id)) }
    }

    suspend fun getReactionByUnicode(unicode: Emoji): IReaction? = suspendCoroutine {cont ->
        RequestBuffer.request { cont.resume(message.getReactionByUnicode(unicode)) }
    }

    suspend fun getReactionByUnicode(unicode: String): IReaction? = suspendCoroutine {cont ->
        RequestBuffer.request { cont.resume(message.getReactionByUnicode(unicode)) }
    }

    fun removeAllReactions() = async {
        RequestBuffer.request { message.removeAllReactions() }.get()
    }

    fun removeReaction(user: IUser, emoji: Emoji) = async {
        RequestBuffer.request { message.removeReaction(user, emoji) }.get()
    }

    fun removeReaction(user: IUser, emoji: ReactionEmoji) = async {
        RequestBuffer.request { message.removeReaction(user, emoji) }.get()
    }

    fun removeReaction(user: IUser?, emoji: String?) = async {
        RequestBuffer.request { message.removeReaction(user, emoji) }.get()
    }

    fun copy(): DiscordMessage = DiscordMessage(message.copy())

    suspend fun removeReaction(author: IUser, reaction: IReaction){
        async {
            RequestBuffer.request {
                message.removeReaction(author, reaction)
            }.get()
        }.await()
    }

    suspend fun removeReaction(author: IUser, emoij: IEmoji){
        RequestBuffer.request { message.removeReaction(author, emoij) }
    }

    suspend fun edit(apply: EmbedContainer.() -> Unit) : DiscordMessage{
        val container = EmbedContainer()
        container.apply()
        return edit(container())
    }

    suspend fun edit(content: String) : DiscordMessage = suspendCoroutine{ cont ->
        RequestBuffer.request { cont.resume(DiscordMessage(message.edit(content))) }
    }

    suspend fun edit(embed: EmbedObject) : DiscordMessage = suspendCoroutine{ cont ->
        RequestBuffer.request { cont.resume(DiscordMessage(message.edit(embed))) }
    }

    fun deleteLater(amount: Long = 10, time: TimeUnit = TimeUnit.SECONDS){
        message.autoDelete(amount, time)
    }

    suspend fun delete() {
        async {
            RequestBuffer.request { message.delete() }.get()
        }.await()
    }


}
