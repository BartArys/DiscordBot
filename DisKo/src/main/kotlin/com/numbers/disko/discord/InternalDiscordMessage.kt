package com.numbers.disko.discord

import com.numbers.disko.EmbedContainer
import com.numbers.disko.discord.extensions.asDiscordMessage
import com.numbers.disko.discord.extensions.autoDelete
import com.numbers.disko.discord.extensions.executeAsync
import com.vdurmont.emoji.Emoji
import kotlinx.coroutines.experimental.Deferred
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.MessageTokenizer
import java.time.Instant
import java.util.concurrent.TimeUnit

internal class InternalDiscordMessage(override val message: IMessage) : DiscordMessage {

    override val attachments: MutableList<IMessage.Attachment> = message.attachments!!
    override val author = message.author!!
    override val channel = message.channel!!
    override val channelMentions = message.channelMentions.toList()
    override val content = message.content!!
    override val editedTimeStamp: Instant? get() = message.editedTimestamp.orElse(null)
    override val embeds = message.embeds.toList()
    override val formattedContent = message.formattedContent!!
    override val guild = message.guild!!
    override val longId = message.longID
    override val mentions = message.mentions.toList()
    override val reactions = message.reactions.toList()
    override val roleMentions: MutableList<IRole> = message.roleMentions!!
    override val type = message.type!!
    override val webhookLongId = message.webhookLongID
    override val isDeleted = message.isDeleted
    override val isPinned = message.isPinned
    override val isSystemMessage = message.isSystemMessage
    override val mentionsEveryone = message.mentionsEveryone()
    override val mentionsHere = message.mentionsHere()
    override val tokenized: MessageTokenizer get() = message.tokenize()
    override val client = message.client!!
    override val shard = message.shard!!

    override fun addReaction(emoji: ReactionEmoji) = { message.addReaction(emoji) }.executeAsync()

    override fun addReaction(emoji: Emoji) = { message.addReaction(emoji) }.executeAsync()

    override fun addReaction(emoji: IEmoji) = { message.addReaction(emoji) }.executeAsync()

    override fun addReaction(reaction: IReaction) = { message.addReaction(reaction) }.executeAsync()

    override fun edit(content: String, embed: EmbedObject) = { message.edit(content, embed) }.executeAsync()

    override fun getReactionByEmoji(emoji: IEmoji) = { message.getReactionByEmoji(emoji) }.executeAsync()

    override fun getReactionByEmoji(emoji: ReactionEmoji) = { message.getReactionByEmoji(emoji) }.executeAsync()

    override fun getReactionByID(id: Long) = { message.getReactionByID(id) }.executeAsync()

    override fun getReactionByUnicode(unicode: Emoji) = { message.getReactionByUnicode(unicode) }.executeAsync()

    override fun getReactionByUnicode(unicode: String) = { message.getReactionByUnicode(unicode) }.executeAsync()

    override fun removeAllReactions() = { message.removeAllReactions() }.executeAsync()

    override fun removeReaction(user: IUser, emoji: Emoji) = { message.removeReaction(user, emoji) }.executeAsync()

    override fun removeReaction(user: IUser, emoji: ReactionEmoji) = { message.removeReaction(user, emoji) }.executeAsync()

    override fun removeReaction(user: IUser, emoji: String) = { message.removeReaction(user, emoji) }.executeAsync()

    override fun copy(): InternalDiscordMessage = InternalDiscordMessage(message.copy())

    override fun removeReaction(author: IUser, reaction: IReaction) = { message.removeReaction(author, reaction) }.executeAsync()

    override fun removeReaction(author: IUser, emoji: IEmoji) = { message.removeReaction(author, emoji) }.executeAsync()

    override fun edit(apply: EmbedContainer.() -> Unit): Deferred<DiscordMessage> = EmbedContainer().also(apply).let { edit(it()) }

    override fun edit(content: String) = { message.edit(content).asDiscordMessage }.executeAsync()

    override fun edit(embed: EmbedObject) = { message.edit(embed).asDiscordMessage }.executeAsync()

    override fun deleteAfter(amount: Long, time: TimeUnit) = message.autoDelete(amount, time)

    override fun deleteLater() = message.autoDelete()

    override fun delete() = { message.delete() }.executeAsync()
}