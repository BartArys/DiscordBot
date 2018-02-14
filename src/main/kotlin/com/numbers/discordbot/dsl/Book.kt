package com.numbers.discordbot.dsl

import com.vdurmont.emoji.EmojiManager
import kotlinx.coroutines.experimental.async
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

class Book<T>(val binder: Binder<T>) {

    @Volatile
    private var subscribed = false

    private var message: DiscordMessage? = null

    private var onDeleteTrigger : (Book<T>) -> Unit = {}

    var index : Int by object : ObservableProperty<Int>(0){
        override fun beforeChange(property: KProperty<*>, oldValue: Int, newValue: Int): Boolean {
            return when {
                newValue >= binder.pageCount -> {
                    index = 0
                    false
                }
                newValue < 0 -> {
                    index = binder.pageCount
                    false
                }
                else -> true
            }
        }
    }

    val currentPage : EmbedObject get() { return binder.offset(binder.pages[index]) }

    suspend fun addControls(){
        Emoji.values().sortedBy { it.order }.forEach {
            message!!.addReaction(EmojiManager.getByUnicode(it.unicode)).await()
        }
    }

    enum class Emoji(val order : Int, val unicode: String){
        DELETE(0, "\u274C"),
        PREVIOUS_PAGE(1,"\u25C0"),
        NEXT_PAGE(2, "\u25B6")
    }

    @EventSubscriber
    fun onReaction(event: ReactionAddEvent) = outsideReaction(event){
        val emoji = reaction.emoji.name
        when(emoji){
            Emoji.DELETE.unicode -> {
                unsubscribe()
                onDeleteTrigger(this@Book)
                async { message!!.delete() }
            }
            Emoji.PREVIOUS_PAGE.unicode -> previousPage()
            Emoji.NEXT_PAGE.unicode -> nextPage()
        }

    }

    fun subscribe(){
        synchronized(subscribed){
            if(!subscribed){
                message!!.client.dispatcher.registerListener(this)
                subscribed = true
            }
        }
    }

    fun onDelete(block : (Book<T>) -> Unit){
        onDeleteTrigger = block
    }

    fun publish(message : DiscordMessage) = async {
        this@Book.message = message
        addControls()
        subscribe()
        refresh()
    }

    fun unsubscribe(){
        synchronized(subscribed){
            if(subscribed){
                message!!.client.dispatcher.unregisterListener(this)
                subscribed = false
            }
        }
    }

    fun nextPage() = index++

    fun previousPage() = index--

    suspend fun refresh(){
        message!!.edit(currentPage)
    }

}

fun DiscordMessage.publish(book: Book<*>) = book.publish(this@publish)

internal fun outsideReaction(event: ReactionAddEvent, onPass: ReactionEvent.() -> Unit){
    if(event.user.stringID != event.client.ourUser.stringID) {
        event.onPass()
        val message = DiscordMessage(event.reaction.message)
        message.removeReaction(event.user, event.reaction.emoji)
    }
}

data class Binder<T>(
        val pageCount: Int,
        val offset: (T) -> EmbedObject,
        val pages : List<T>
)