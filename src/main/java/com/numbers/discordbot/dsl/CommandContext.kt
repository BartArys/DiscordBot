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

data class CommandContext(val services: Services, val event: MessageReceivedEvent, val args : CommandArguments){
    val channel = event.channel!!
    val client = event.client!!
    val author = event.author!!
    val message : DiscordMessage= InternalDiscordMessage(event.message!!)
    val guild : IGuild? = event.guild
    val bot = event.client.ourUser!!

    inline fun respond(container: EmbedContainer.() -> Unit) : Deferred<DiscordMessage> {
        val embed = embed(container)
        return respond(embed, embed.autoDelete)
    }

    inline fun respondError(apply: EmbedContainer.() -> Unit) : Deferred<DiscordMessage> {
        val container = EmbedContainer()
        container.apply()
        container.color = Color.red
        return respond(container(), container.autoDelete)
    }

    inline fun respondScreen(loadingMessage : String = "building...", block: ScreenBuilder.() -> Unit) : Deferred<DiscordMessage> {
        val builder = ScreenBuilder()
        block.invoke(builder)
        return async {
            val base = respond(loadingMessage).await()
            builder.build(base)
        }
    }

    fun respond(container: EmbedContainer, autoDelete: Boolean = false) : Deferred<DiscordMessage> {
        return if(container.file != null) respond(container(), container.file!!, container.fileName!!, autoDelete)
        else respond(container(), autoDelete)
    }

    fun respond(content: String, autoDelete: Boolean = false) = async {
        if(autoDelete) message.deleteLater()
            val request = RequestBuffer.IRequest<DiscordMessage> {
                InternalDiscordMessage(channel.sendMessage(content)).also {
                    if(autoDelete) message.deleteLater()
                }
            }

            RequestBuffer.request(request).get()
    }

    fun respond(embed: EmbedObject, file: InputStream, fileName: String, autoDelete: Boolean = false) : Deferred<DiscordMessage> = async {
        val request = RequestBuffer.IRequest {
            InternalDiscordMessage(channel.sendFile(embed, file, fileName)).also { if(autoDelete) it.deleteLater() }
        }

        RequestBuffer.request(request).get()
    }

    fun respond(embed: EmbedObject, autoDelete: Boolean = false) = async {
        val request = RequestBuffer.IRequest<DiscordMessage> {
            InternalDiscordMessage(channel.sendMessage(embed)).also {
                if(autoDelete) it.deleteLater()
            }
        }

         RequestBuffer.request(request).get()
    }

}

fun<T> Iterable<T>.bind(apply: ItemEmbedContainer<T>.() -> Unit) : Book<T> {

    val binder = Binder(
            this.count(),
            {
                val container = ItemEmbedContainer(it)
                container.apply()
                container()
            },
            this.toList()
    )

    return Book(binder)
}

data class Command(
        val usage: String,
        var arguments: List<Argument> = emptyList(),
        var handler : (suspend (CommandContext) -> Unit)? = null,
        var info : CommandInfo = CommandInfo(),
        var permissions: List<String> = emptyList()
){
    fun arguments(vararg arguments: Argument){
        this.arguments = arguments.toList()
    }

    fun execute(handler: suspend CommandContext.() -> Unit){
        this.handler = handler
    }

    fun permissions(vararg permissions: String){
        this.permissions = permissions.toList()
    }

    inline fun info(info: CommandInfo.() -> Unit){
        this.info.info()
    }
}



data class CommandInfo(var description: String? = null, var name: String? = null)

data class CommandsContainer(var commands: MutableList<Command> = mutableListOf()){
    val subCommands = mutableListOf<String>()

    fun simpleCommand(usage: String, create: suspend CommandContext.() -> Unit) : Command{
        return command(usage){
            execute {
                this.create()
            }
        }
    }

    fun command(usage: String) = subCommands.add(usage)

     inline fun command(usage: String, create: (Command.() -> Unit)) : Command {
        val command = Command(usage)
        command.create()
        commands.add(command)
        subCommands.map { command.copy(usage = it) }.forEach { commands.add(it) }
        subCommands.clear()

        return command
    }
}


inline fun commands(create: CommandsContainer.() -> Unit) : CommandsContainer{
    val commandsContainer = CommandsContainer()
    commandsContainer.create()
    return commandsContainer
}


