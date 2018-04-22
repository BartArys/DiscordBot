package com.numbers.discordbot.dsl.gui2

import com.numbers.discordbot.dsl.Command
import com.numbers.discordbot.dsl.GuildCommand
import com.numbers.discordbot.dsl.IEmbedContainer
import com.numbers.discordbot.dsl.SetupContext
import com.numbers.discordbot.dsl.discord.DiscordMessage
import com.numbers.discordbot.dsl.guard.canMessage
import com.numbers.discordbot.dsl.guard.canRemoveEmoji
import com.numbers.discordbot.dsl.guard.guard
import com.numbers.discordbot.dsl.gui.builder.Emote
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser
import java.awt.Color
import java.io.InputStream
import java.time.Instant
import java.util.concurrent.TimeUnit

class ScreenBuilder(val guild: IGuild) : Controllable<Screen>, IEmbedContainer {
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

    private val messageListeners: MutableList<IListener<MessageReceivedEvent>> = mutableListOf()
    private val screenItems = mutableListOf<ScreenItem>()
    private val controls = mutableListOf<Pair<String, (Screen, ReactionAddEvent) -> Unit>>()
    private val refreshListeners: MutableList<Screen.() -> Unit> = mutableListOf()

    lateinit var screen: Screen

    override fun addControl(controlTrigger: String, block: (Screen, ReactionAddEvent) -> Unit) {
        controls.add(controlTrigger to block)
    }

    fun onRefresh(block: Screen.() -> Unit) {
        refreshListeners.add(block)
    }

    inline fun property(property: ScreenBuilder.() -> Unit) {
        property.invoke(this)
    }

    fun add(item: ScreenItem) = screenItems.add(item)

    fun build(message: DiscordMessage): DiscordMessage {
        return Screen(message, screenItems, refreshListeners, controls, messageListeners).also {
            it.invalidated(null)

            screen = it
        }
    }

    fun delete() = screen.delete()

    override fun removeControl(controlTrigger: String) {
        controls.removeIf { it.first == controlTrigger }
    }

    inline fun addCommand(usage: String, block: Command.() -> Unit): IListener<MessageReceivedEvent> {
        val command = GuildCommand(usage, guild = guild)
        command.apply(block)
        return addCommand(command)
    }

    fun addCommand(command: Command): IListener<MessageReceivedEvent> {
        return SetupContext.sharedContext.compile(GuildCommand(command.usage, command.arguments, command.handler, command.info, command.permissions, guild)).also { addListener(it) }
    }

    fun addListener(listener: IListener<MessageReceivedEvent>) {
        messageListeners.add(listener)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ScreenBuilder::class.java)
    }
}

inline val deletable: ScreenBuilder.() -> Unit get() = { controls { forEmote(Emote.close) { screen, _ -> screen.delete() } } }
fun authorDeletable(author: IUser): ScreenBuilder.() -> Unit = {
    controls {
        forEmote(Emote.close) { screen, event ->
            if (event.user.longID == author.longID) {
                screen.delete()
            }
        }
    }
}

class Screen(
        private val discordMessage: DiscordMessage,
        private val screenItems: List<ScreenItem>,
        private val refreshListeners: List<Screen.() -> Unit>,
        private val controls: MutableList<Pair<String, (Screen, ReactionAddEvent) -> Unit>>,
        private val messageListeners: MutableList<IListener<MessageReceivedEvent>> = mutableListOf()
) : IEmbedContainer, InvalidationListener, DiscordMessage by discordMessage, Controllable<Screen>, IListener<ReactionAddEvent> {
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

        messageListeners.forEach { client.dispatcher.registerListener(it) }
    }

    override fun handle(event: ReactionAddEvent) {
        if (event.user.stringID == client.ourUser.stringID) return
        if (event.message.stringID != message.stringID) return

        val listeners = controls.filter { it.first == event.reaction.emoji.name }
        if (listeners.isNotEmpty()) {
            listeners.forEach {
                it.second.invoke(this, event)
            }
            refresh()
        }

        channel.guard({ canRemoveEmoji }) { removeReaction(event.user, event.reaction) }
    }

    inline fun addCommand(usage: String, block: Command.() -> Unit): IListener<MessageReceivedEvent> {
        val command = GuildCommand(usage, guild = message.guild)
        command.apply(block)
        return SetupContext.sharedContext.compile(command).also { addMessageListener(it) }
    }

    fun addMessageListener(listener: IListener<MessageReceivedEvent>) {
        messageListeners.add(listener)
        client.dispatcher.registerListener(listener)
    }

    override fun invalidated(observable: Observable?) = refresh()

    fun refresh() {
        channel.guard({ canMessage }) {
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
                }
            }
        }
    }

    fun unregister() {
        message.client.dispatcher.unregisterListener(this)
        messageListeners.forEach { client.dispatcher.unregisterListener(it) }
    }

    fun register() {
        unregister()
        message.client.dispatcher.registerListener(this)
        messageListeners.forEach { client.dispatcher.registerListener(it) }
    }

    private fun destroy() {
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

    override fun addControl(controlTrigger: String, block: (Screen, ReactionAddEvent) -> Unit) {
        controls.add(controlTrigger to block)
    }

    override fun removeControl(controlTrigger: String) {
        controls.removeIf { it.first == controlTrigger }
    }

    fun removeControls() {
        controls.clear()
        removeAllReactions()
    }
}

interface Controllable<out T> {
    fun addControl(controlTrigger: String, block: (T, ReactionAddEvent) -> Unit)
    fun removeControl(controlTrigger: String)
}

inline fun <T> Controllable<T>.controls(block: ControlsContext<T>.() -> Unit) {
    val context = ControlsContext(this)
    context.apply(block)
}

class ControlsContext<out T>(private val controllable: Controllable<T>) {
    fun forEmote(controlTrigger: String, block: (T, event: ReactionAddEvent) -> Unit) {
        controllable.addControl(controlTrigger, block)
    }
}