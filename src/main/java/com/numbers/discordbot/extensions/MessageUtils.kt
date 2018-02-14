package com.numbers.discordbot.extensions

import com.numbers.discordbot.message.NavigateAbleMessage
import com.numbers.discordbot.message.PaginatedMessage
import com.numbers.discordbot.message.RoundRobinPaginatedMessage
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.obj.Embed
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.RequestBuilder
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.suspendCoroutine


class MessageUtils {

    companion object {
        val defaultService = Executors.newSingleThreadScheduledExecutor { Thread(it,"AUTO-DELETE SCHEDULER") }!!
    }
}

fun IMessage.autoDelete(after: Long = 10, time: TimeUnit = TimeUnit.SECONDS, service: ScheduledExecutorService = MessageUtils.defaultService) {
    service.schedule({ RequestBuffer.request { this.delete() } }, after, time)
}

suspend fun IChannel.sendMessageAsync(embed: EmbedObject) : IMessage = suspendCoroutine { continuation -> RequestBuffer.request { continuation.resume(this.sendMessage(embed))} }


fun List<Embed.EmbedField>.toPaginatedMessage(message: IMessage, title: Embed.EmbedField? = null, footer: Embed.EmbedField? = null, ondelete: (IMessage) -> Unit = {}) : RoundRobinPaginatedMessage {
    RequestBuilder(message.client).shouldBufferRequests(true).doAction {
        message.addReaction(ReactionEmoji.of(PaginatedMessage.Emoji.PREVIOUS_PAGE.unicode))
        true
    }.then {
        message.addReaction(ReactionEmoji.of(PaginatedMessage.Emoji.NEXT_PAGE.unicode))
    }.then {
        message.addReaction(ReactionEmoji.of(NavigateAbleMessage.Emoji.CROSS.unicode))
    }.execute()

    return object : RoundRobinPaginatedMessage{
        override val message: IMessage
            get() = message


        override fun refresh() {
            if(message.isDeleted) return

            val builder = EmbedBuilder()

            title?.let { builder.appendField(title) }
            builder.appendField(get(page))
            footer?.let { builder.appendField(it) }

            RequestBuffer.request { message.edit(builder.build()) }
        }

        override fun close() {
            super.close()
            ondelete(message)
        }

        override var page: Int = 0
        override val pageCount: Int = count()
    }.also { it.refresh() }
}