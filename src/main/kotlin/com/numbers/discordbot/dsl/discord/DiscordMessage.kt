package com.numbers.discordbot.dsl.discord

import com.numbers.discordbot.dsl.EmbedContainer
import com.vdurmont.emoji.Emoji
import kotlinx.coroutines.experimental.Deferred
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.IShard
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.MessageTokenizer
import java.time.Instant
import java.util.concurrent.TimeUnit

interface DiscordMessage {
    val message: IMessage
    val attachments: MutableList<IMessage.Attachment>
    val author: IUser
    val channel: IChannel
    val channelMentions: List<IChannel>
    val content: String
    val editedTimeStamp: Instant?
    val embeds: List<IEmbed>
    val formattedContent: String
    val guild: IGuild?
    val longId: Long
    val mentions: List<IUser>
    val reactions: List<IReaction>
    val roleMentions: MutableList<IRole>
    val type: IMessage.Type
    val webhookLongId: Long
    val isDeleted: Boolean
    val isPinned: Boolean
    val isSystemMessage: Boolean
    val mentionsEveryone: Boolean
    val mentionsHere: Boolean
    val tokenized: MessageTokenizer
    val client: IDiscordClient
    val shard: IShard

    fun addReaction(emoji: ReactionEmoji): Deferred<Unit>
    fun addReaction(emoji: Emoji): Deferred<Unit>
    fun addReaction(emoji: IEmoji): Deferred<Unit>
    fun addReaction(reaction: IReaction): Deferred<Unit>

    fun edit(content: String, embed: EmbedObject): Deferred<IMessage>
    fun edit(content: String): Deferred<DiscordMessage>
    fun edit(embed: EmbedObject): Deferred<DiscordMessage>

    fun getReactionByID(id: Long): Deferred<IReaction?>
    fun getReactionByEmoji(emoji: IEmoji): Deferred<IReaction?>
    fun getReactionByEmoji(emoji: ReactionEmoji): Deferred<IReaction?>
    fun getReactionByUnicode(unicode: Emoji): Deferred<IReaction?>
    fun getReactionByUnicode(unicode: String): Deferred<IReaction?>

    fun removeReaction(user: IUser, emoji: Emoji): Deferred<Unit>
    fun removeReaction(user: IUser, emoji: ReactionEmoji): Deferred<Unit>
    fun removeReaction(user: IUser, emoji: String): Deferred<Unit>
    fun removeReaction(author: IUser, reaction: IReaction): Deferred<Unit>
    fun removeAllReactions(): Deferred<Unit>

    fun removeReaction(author: IUser, emoji: IEmoji): Deferred<Unit>
    fun edit(apply: EmbedContainer.() -> Unit): Deferred<DiscordMessage>

    fun deleteAfter(amount: Long, time: TimeUnit)
    fun deleteLater()
    fun delete(): Deferred<Unit>

    fun copy(): DiscordMessage
}