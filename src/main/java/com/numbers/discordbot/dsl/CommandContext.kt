package com.numbers.discordbot.dsl

import com.numbers.discordbot.dsl.discord.DiscordMessage
import com.numbers.discordbot.dsl.discord.InternalDiscordMessage
import com.numbers.discordbot.dsl.guard.guard
import com.numbers.discordbot.dsl.gui2.ScreenBuilder
import com.numbers.discordbot.dsl.permission.Permission
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.io.InputStream
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.util.RequestBuffer
import java.awt.Color

class CommandContext(services: Services, val event: MessageReceivedEvent, val args: CommandArguments) {
    val channel = event.channel!!
    val client = event.client!!
    val author = event.author!!
    val message: DiscordMessage = InternalDiscordMessage(event.message!!)
    val guild: IGuild? = event.guild
    val bot = event.client.ourUser!!

    val services : Services by lazy {
        services.copy(context = this)
    }

    inline fun respond(autoDelete: Boolean = false, container: EmbedContainer.() -> Unit): Deferred<DiscordMessage> {
        val embed = embed(container)
        return respond(embed.autoDelete || autoDelete, embed)
    }

    inline fun respondError(apply: EmbedContainer.() -> Unit): Deferred<DiscordMessage> {
        val container = EmbedContainer()
        container.apply()
        container.color = Color.red
        return respond(container(), container.autoDelete)
    }

    inline fun respondScreen(loadingMessage: String = "building...", block: ScreenBuilder.() -> Unit): Deferred<DiscordMessage> {
        val builder = ScreenBuilder()
        block.invoke(builder)
        return async {
            val base = respond(loadingMessage).await()
            builder.build(base)
        }
    }

    fun respond(autoDelete: Boolean = false, container: EmbedContainer): Deferred<DiscordMessage> {
        return if (container.file != null) respond(container(), container.file!!, container.fileName!!, autoDelete)
        else respond(container(), autoDelete)
    }

    fun respond(content: String, autoDelete: Boolean = false) = async {
        if (autoDelete) message.deleteLater()
        val request = RequestBuffer.IRequest<DiscordMessage> {
            InternalDiscordMessage(channel.sendMessage(content)).also {
                if (autoDelete) message.deleteLater()
            }
        }

        RequestBuffer.request(request).get()
    }

    fun respond(embed: EmbedObject, file: InputStream, fileName: String, autoDelete: Boolean = false): Deferred<DiscordMessage> = async {
        val request = RequestBuffer.IRequest {
            InternalDiscordMessage(channel.sendFile(embed, file, fileName)).also { if (autoDelete) it.deleteLater() }
        }

        RequestBuffer.request(request).get()
    }

    fun respond(embed: EmbedObject, autoDelete: Boolean = false) = async {
        val request = RequestBuffer.IRequest<DiscordMessage> {
            InternalDiscordMessage(channel.sendMessage(embed)).also {
                if (autoDelete) it.deleteLater()
            }
        }

        RequestBuffer.request(request).get()
    }

}

data class Command(
        val usage: String,
        var arguments: List<Argument> = emptyList(),
        var handler: (suspend (CommandContext) -> Unit)? = null,
        var info: CommandInfo = CommandInfo(),
        var permissions: List<Class<out Permission>> = emptyList()
) {
    fun arguments(vararg arguments: Argument) {
        this.arguments = arguments.toList()
    }

    fun execute(handler: suspend CommandContext.() -> Unit) {
        this.handler =  handler
    }

    inline fun execute(crossinline guard: CommandContext.() -> Boolean, noinline handler: CommandContext.() -> Unit) {
        this.handler = { it.guard({ guard(it) }) { handler(it) } }
    }

    fun permissions(vararg permissions: Permission) {
        this.permissions = permissions.toList().map { it::class.java }
    }

    inline fun info(info: CommandInfo.() -> Unit) {
        this.info.info()
    }
}


data class CommandInfo(var description: String? = null, var name: String? = null)

data class CommandsContainer(var commands: MutableList<Command> = mutableListOf()) {
    val subCommands = mutableListOf<String>()

    fun simpleCommand(usage: String, create: suspend CommandContext.() -> Unit): Command {
        return command(usage) {
            execute {
                this.create()
            }
        }
    }

    inline fun simpleCommand(usage: String, crossinline guard: CommandContext.() -> Boolean, crossinline create: CommandContext.() -> Unit): Command {
        return command(usage) {
            execute(guard) {
                this.create()
            }
        }
    }

    fun command(usage: String) = subCommands.add(usage)

    inline fun command(usage: String, create: (Command.() -> Unit)): Command {
        val command = Command(usage)
        command.create()
        commands.add(command)
        subCommands.map { command.copy(usage = it) }.forEach { commands.add(it) }
        subCommands.clear()

        return command
    }
}


inline fun commands(create: CommandsContainer.() -> Unit): CommandsContainer {
    val commandsContainer = CommandsContainer()
    commandsContainer.create()
    return commandsContainer
}


