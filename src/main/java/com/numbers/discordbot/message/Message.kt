package com.numbers.discordbot.message

import com.vdurmont.emoji.EmojiManager
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.RequestBuffer

interface DisplayMessage {

    val message: IMessage

    val needsRefresh: Boolean
        get() {
            return true
        }

    fun refresh()

    fun close()
}

interface NavigateAbleMessage : DisplayMessage, IListener<ReactionAddEvent> {

    fun next()

    fun previous()

    fun init() {
        this.message.client.dispatcher.registerListener(this)
        RequestBuffer.request {
            message.addReaction(EmojiManager.getByUnicode(Emoji.CROSS.unicode))
        }
    }

    override fun close() {
        this.message.client.dispatcher.unregisterListener(this)
        RequestBuffer.request {
            this.message.delete()
        }
    }

    override fun handle(event: ReactionAddEvent) {
        if (message.isDeleted) return
        if (event.user.stringID == event.client.ourUser.stringID) return
        if (event.message.stringID != message.stringID) return

        RequestBuffer.request { event.reaction.message.removeReaction(event.user, event.reaction) }

        if (event.reaction.emoji?.name == Emoji.CROSS.unicode) {
            this.close()
        }
    }

    enum class Emoji(val unicode: String) {
        CROSS("\u274C")
    }

}

interface PaginatedMessage : NavigateAbleMessage {

    var page: Int

    val pageCount: Int

    override fun next() {
        page = Math.max(page + 1, pageCount)
        refresh()
    }

    override fun previous() {
        page = Math.min(page - 1, 0)
        refresh()
    }

    fun toFirst() {
        page = 0
    }

    fun toLast() {
        page = pageCount
    }

    override fun handle(event: ReactionAddEvent) {
        super.handle(event)

        if (message.isDeleted) return
        if (event.user.stringID == event.client.ourUser.stringID) return
        if (event.message.stringID != message.stringID) return

        when (event.reaction.emoji?.name) {
            Emoji.PREVIOUS_PAGE.unicode -> previous()
            Emoji.NEXT_PAGE.unicode -> next()
        }
    }


    enum class Emoji(val order: Int, val unicode: String) {
        PREVIOUS_PAGE(1, "\u25C0"),
        NEXT_PAGE(2, "\u25B6")
    }

}

interface RoundRobinPaginatedMessage : PaginatedMessage {

    override fun next() {
        page = (page + 1) % pageCount
        RequestBuffer.request { refresh() }
    }

    override fun previous() {
        if (page - 1 < 0) {
            page = pageCount
        } else {
            page -= 1
        }
        RequestBuffer.request { refresh() }
    }


}