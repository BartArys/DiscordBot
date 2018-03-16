package com.numbers.discordbot.dsl.gui2

import com.numbers.discordbot.dsl.IEmbedContainer
import com.numbers.discordbot.dsl.discord.DiscordMessage
import com.numbers.discordbot.dsl.gui.builder.Emote
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import java.awt.Color
import java.io.InputStream
import java.time.Instant
import java.util.concurrent.TimeUnit

class ScreenBuilder : Controllable<Screen>, IEmbedContainer {
    override var description: String? = null
    override var image: String? = null
    override var title: String? = null
    override var thumbnail: String? = null
    override var color: Color? = null
    override var url: String? = null
    override var timeStamp: Instant? = null
    override var file: InputStream? = null
    override var fileName: String? = null
    override var autoDelete: Boolean = false
    override var authorUrl: String? = null
    override var authorName: String? = null
    override var authorIcon: String? = null
    override var footerIcon: String? = null
    override var footerText: String? = null

    var deleteAble : Boolean = false

    private val screenItems = mutableListOf<ScreenItem>()
    private val controls = mutableListOf<Pair<String, (Screen) -> Unit>>()
    private val refreshListeners : MutableList<Screen.() -> Unit> = mutableListOf()

    override fun addControl(controlTrigger: String, block: (Screen) -> Unit) {
        controls.add(controlTrigger to block)
    }

    fun onRefresh(block: Screen.() -> Unit){
        refreshListeners.add(block)
    }

    fun add(item : ScreenItem) = screenItems.add(item)

    fun build(message: DiscordMessage) : DiscordMessage{
        return Screen(message, screenItems, refreshListeners, controls).also {
            it.controls {
                if(autoDelete) forEmote(Emote.close) { it.delete() }
            }
            it.invalidated(null)
        }
    }

    override fun removeControl(controlTrigger: String) {
        controls.removeIf { it.first == controlTrigger }
    }

}

class Screen(
        private val discordMessage : DiscordMessage,
        private val screenItems: List<ScreenItem>,
        private val refreshListeners : List<Screen.() -> Unit>,
        private val controls : MutableList<Pair<String, (Screen) -> Unit>>
) : IEmbedContainer, InvalidationListener, DiscordMessage by discordMessage, IListener<ReactionAddEvent>, Controllable<Screen> {
    override var description: String? = null
    override var image: String? = null
    override var title: String? = null
    override var thumbnail: String? = null
    override var color: Color? = null
    override var url: String? = null
    override var timeStamp: Instant? = null
    override var file: InputStream? = null
    override var fileName: String? = null
    override var autoDelete: Boolean = false
    override var authorUrl: String? = null
    override var authorName: String? = null
    override var authorIcon: String? = null
    override var footerIcon: String? = null
    override var footerText: String? = null

    init {
        screenItems.forEach { it.addListener(this) }
        message.client.dispatcher.registerListener(this)
        async {
            controls.map { it.first }.distinct().forEach { addReaction(ReactionEmoji.of(it)).await() }
        }
    }

    override fun handle(event: ReactionAddEvent) {
        if(event.user.stringID == client.ourUser.stringID) return
        if(event.message.stringID != message.stringID) return

        val listeners = controls.filter { it.first == event.reaction.emoji.name }
        if(listeners.isNotEmpty()){
            listeners.forEach {
                it.second.invoke(this)
            }
            refresh()
        }

        removeReaction(event.user, event.reaction)
    }

    override fun invalidated(observable: Observable?) = refresh()

    fun refresh() {
         async {
            refreshListeners.forEach { it(this@Screen) }
            edit {
                description = this@Screen.description
                image = this@Screen.image
                title = this@Screen.title
                thumbnail = this@Screen.thumbnail
                color = this@Screen.color
                url = this@Screen.url
                timeStamp = this@Screen.timeStamp
                file = this@Screen.file
                fileName = this@Screen.fileName
                autoDelete = this@Screen.autoDelete
                authorUrl = this@Screen.authorUrl
                authorName = this@Screen.authorName
                authorIcon = this@Screen.authorIcon
                footerIcon = this@Screen.footerIcon
                footerText = this@Screen.footerText

                screenItems.flatMap { it.render() }.forEach { this.embedField(it) }
            }.await()
        }
    }

    fun unregister() {
        message.client.dispatcher.unregisterListener(this)
    }

    fun register() {
        unregister()
        message.client.dispatcher.registerListener(this)
    }

    private fun destroy(){
        unregister()
        screenItems.forEach {
            it.removeListener(this)
            it.destroy()
        }
    }

    override fun delete(): Deferred<Unit> {
        destroy()
        return discordMessage.delete()
    }

    override fun deleteAfter(amount: Long, time: TimeUnit) {
        destroy()
        discordMessage.deleteAfter(amount, time)
    }

    override fun deleteLater() {
        destroy()
        discordMessage.deleteLater()
    }

    override fun addControl(controlTrigger: String, block: (Screen) -> Unit) {
        controls.add(controlTrigger to block)
    }

    override fun removeControl(controlTrigger: String) {
        controls.removeIf { it.first == controlTrigger }
    }

    fun removeControls(){
        controls.clear()
        removeAllReactions()
    }
}

interface Controllable<out T>{
    fun addControl(controlTrigger: String, block: (T) -> Unit)
    fun removeControl(controlTrigger: String)
}

inline fun<T> Controllable<T>.controls(block: ControlsContext<T>.() -> Unit){
    val context = ControlsContext(this)
    context.apply(block)
}

class ControlsContext<out T>(private val controllable : Controllable<T>){
    fun forEmote(controlTrigger: String, block: (T) -> Unit) {
        controllable.addControl(controlTrigger, block)
    }
}