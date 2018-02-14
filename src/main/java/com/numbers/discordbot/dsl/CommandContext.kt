package com.numbers.discordbot.dsl

import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.service.Permission
import kotlinx.io.InputStream
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RequestBuffer
import java.awt.Color
import kotlin.coroutines.experimental.suspendCoroutine

data class CommandContext(val services: Services, val event: MessageReceivedEvent, val args : CommandArguments){
    val channel = event.channel!!
    val client = event.client!!
    val author = event.author!!
    val message = DiscordMessage(event.message!!)
    val guild = event.guild!!
    val bot = event.client.ourUser

    suspend fun respond(container: EmbedContainer.() -> Unit) : DiscordMessage {
        val embed = embed(container)
        return respond(embed, embed.autoDelete)
    }

    suspend fun respondError(apply: EmbedContainer.() -> Unit) : DiscordMessage {
        val container = EmbedContainer()
        container.apply()
        container.color = Color.red
        return respond(container(), container.autoDelete)
    }

    suspend fun respond(container: EmbedContainer, autoDelete: Boolean = false) : DiscordMessage {
        if(container.file != null) return respond(container(), container.file!!, container.fileName!!, autoDelete)
        return respond(container(), autoDelete)
    }
    suspend fun respond(content: String, autoDelete: Boolean = false) : DiscordMessage = suspendCoroutine { cont -> RequestBuffer.request {
        val message = DiscordMessage(channel.sendMessage(content))
        cont.resume(message)
        if(autoDelete) message.deleteLater()
    } }

    suspend fun respond(embed: EmbedObject, file: InputStream, fileName: String, autoDelete: Boolean = false) : DiscordMessage = suspendCoroutine { cont ->  RequestBuffer.request {
        val message = DiscordMessage(channel.sendFile(embed, file, fileName))
        cont.resume(message)
        if(autoDelete) message.deleteLater()
    } }

    suspend fun respond(embed: EmbedObject, autoDelete: Boolean = false) : DiscordMessage = suspendCoroutine { cont ->  RequestBuffer.request {
        val message = DiscordMessage(channel.sendMessage(embed))
        cont.resume(message)
        if(autoDelete) message.deleteLater()
    } }


    fun Iterable<String>.toEmbeds() : Iterable<EmbedObject>{
        return this.map { content -> com.numbers.discordbot.dsl.embed { description = content }() }
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
        var permissions: List<Permission> = emptyList()
){
    fun arguments(vararg arguments: Argument){
        this.arguments = arguments.toList()
    }

    fun execute(handler: suspend CommandContext.() -> Unit){
        this.handler = handler
    }

    fun permissions(vararg permissions: Permission){
        this.permissions = permissions.toList()
    }

    fun info(info: CommandInfo.() -> Unit){
        this.info.info()
    }
}

data class CommandInfo(var description: String? = null, var name: String? = null)

data class CommandsContainer(var commands: MutableList<Command> = mutableListOf()){
    private val subCommands = mutableListOf<String>()

    fun simpleCommand(usage: String, create: suspend CommandContext.() -> Unit) : Command{
        return command(usage){
            execute {
                this.create()
            }
        }
    }

    fun command(usage: String, create: (Command.() -> Unit)? = null) : Command {
        val command = Command(usage)
        if (create != null) {
            command.create()
            commands.add(command)
            subCommands.map {
                command.copy(usage = it)
            }.forEach { commands.add(it) }
            subCommands.clear()
        }else{
            subCommands.add(command.usage)
        }

        return command
    }
}


fun commands(create: CommandsContainer.() -> Unit) : CommandsContainer{
    val commandsContainer = CommandsContainer()
    commandsContainer.create()
    return commandsContainer
}


