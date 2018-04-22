package com.numbers.discordbot.dsl

import com.numbers.discordbot.dsl.discord.DiscordMessage
import com.numbers.discordbot.dsl.discord.InternalDiscordMessage
import com.numbers.discordbot.dsl.gui2.ScreenBuilder
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

    val respond: CommandResponse get() {
        return CommandResponse(EmbedContainer(), this)
    }

    fun respond(container: EmbedContainer): Deferred<DiscordMessage> {
        return if (container.file != null) respond(container(), container.file!!, container.fileName!!, container.autoDelete)
        else respond(container(), container.autoDelete)
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

class CommandResponse(val embed: EmbedContainer, val context: CommandContext) {

    val autoDelete : CommandResponse get() {
        embed.autoDelete = true
        return this
    }

    val warning : CommandResponse get() {
        embed.color = Color.yellow
        return this
    }

    val error : CommandResponse get() {
        embed.color  = Color.red
        return this
    }

    inline fun error(container: EmbedContainer.() -> Unit) : Deferred<DiscordMessage> = error.invoke(container)

    inline fun warning(container: EmbedContainer.() -> Unit) : Deferred<DiscordMessage> = warning.invoke(container)

    inline operator fun invoke(container: EmbedContainer.() -> Unit) : Deferred<DiscordMessage> {
        return context.respond(embed)
    }

    operator fun invoke(message: String) : Deferred<DiscordMessage>{
        return context.respond(message)
    }

    inline fun screen(loadingMessage: String = "building...", block: ScreenBuilder.() -> Unit) : Deferred<DiscordMessage> {
        val builder = ScreenBuilder(context.guild!!)
        block.invoke(builder)
        return async {
            val base= invoke(loadingMessage).await()
            builder.build(base)
        }
    }

}

data class CommandInfo(var description: String? = null, var name: String? = null)

data class CommandsContainer(var commands: MutableList<Command> = mutableListOf()) {
    //val subCommands = mutableListOf<String>()

//    inline fun simpleCommand(usage: String, crossinline create: suspend CommandContext.() -> Unit): Command {
//        return command(usage) {
//            execute {
//                this.create()
//            }
//        }
//    }
//
//    inline fun simpleCommand(usage: String, crossinline guard: CommandContext.() -> Boolean, crossinline create: CommandContext.() -> Unit): Command {
//        return command(usage) {
//            execute(guard) {
//                this.create()
//            }
//        }
//    }

    fun command(usage: String) : CommandBuilder {
        val command = UniversalCommand(usage)
        commands.add(command)
        return CommandBuilder(command)
    }

    inline fun command(usage: String, create: (Command.() -> Unit)): Command {
        val command = UniversalCommand(usage)
        command.create()
        commands.add(command)
        commands.filter { it.handler == null }.map { command.copy(usage = it.usage) }.forEach { commands.add(it) }

        return command
    }
}

inline fun commands(create: CommandsContainer.() -> Unit): CommandsContainer {
    val commandsContainer = CommandsContainer()
    commandsContainer.create()
    return commandsContainer
}


