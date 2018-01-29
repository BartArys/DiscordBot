package com.numbers.discordbot.eventHandler

import com.google.inject.Guice
import com.google.inject.Injector
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.guards
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.guard2.Parser
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.service.PermissionsService
import com.numbers.discordbot.service.hasPermission
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

class GuardedEventHandler {

    companion object {

        var injector: Injector = Guice.createInjector()

        val eventInjectors = mutableListOf< suspend (MessageReceivedEvent) -> Any?>()

        fun toListener(`class`: KClass<*>, injector: Injector): List<Any> {
            return `class`.guards.flatMap { pair -> pair.second.map { Parser.parse(it, injector) bindTo  { event, args -> handle(event, args, pair) } } }
        }

        private fun handle(event: MessageReceivedEvent, args: CommandArguments, pair: Pair<KFunction<*>, List<Guard>>){
            val permissionsService = injector.getInstance(PermissionsService::class.java)

            launch {
                val requiredPermissions = pair.first.findAnnotation<Permissions>()?.permission.orEmpty()
                val userPermissions = permissionsService.get(event.author)

                if(requiredPermissions.all { userPermissions.hasPermission(it) } || event.author.stringID == event.client.applicationOwner.stringID){
                    val params: Map<KParameter, Any> = pair.first.parameters.map { param ->
                        eventInjectors.firstOrNull { (param.type.classifier as KClass<*>).isInstance(it(event)) }?.invoke(event)?.let { return@map param to it }
                        when (param.type.classifier) {
                            MessageReceivedEvent::class -> param to event
                            CommandArguments::class -> param to args
                            else -> param to injector.getInstance((param.type.classifier as KClass<*>).java)
                        }
                    }.toMap()
                    pair.first.callBy(params)
                }else{
                    val lackingPermissions = requiredPermissions.map { it.name } - userPermissions.map { it.name }
                    event.channel.sendMessage("lacking required permissions: ${lackingPermissions.map { it.toLowerCase() }}")
                }
            }
        }
    }


}