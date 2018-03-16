package com.numbers.discordbot.dsl

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.slf4j.LoggerFactory
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient

fun setup(setup: SetupContext.() -> Unit) : SetupContext {
    val context = SetupContext(ServicesInjector(), ArgumentContext())
    setup.invoke(context)
    return context
}

internal lateinit var services: Services
internal lateinit var argumentSubstitutes: MutableMap<String,Argument>

data class SetupContext internal constructor(
    val injector: ServicesInjector,
    val argumentContext: ArgumentContext,
    val commands: MutableList<Command> = mutableListOf()
){

    init {
        argumentSubstitutes = argumentContext.argumentSubstitutes
    }

    val commandPackages : MutableList<String> = mutableListOf()

    var token : CharSequence = ""

    inline fun inject(injections: ServicesInjector.() -> Unit) {
        injector.injections()
    }

    inline fun arguments(arguments : ArgumentContext.() -> Unit){
        argumentContext.arguments()
    }

    private fun<K,V> Iterable<Map<K,V>>.flatten() : Map<K,V>{
        val map = mutableMapOf<K,V>()
        this.forEach { map.putAll(it) }
        return map
    }

    infix operator fun plus(container: CommandsContainer){
        commands.addAll(container.commands)
    }

    infix operator fun plusAssign(setup: SetupContext){
        argumentContext.argumentSubstitutes.putAll(setup.argumentContext.argumentSubstitutes)
        argumentContext.tokenSubstitutes.putAll(setup.argumentContext.tokenSubstitutes)
        commands.addAll(setup.commands)
    }

    operator fun invoke(): IDiscordClient = runBlocking {
        val builder = async { ClientBuilder() .withToken(token.toString()).withRecommendedShardCount() }

        services = injector.build()

        val commands =  commandPackages.flatMap { findCommands(it) } + commands
        logger.info("found ${commands.size} commands")
        logger.debug("commands: ", commands)

        val listeners = commands.map {
            val funArgs = if(it.arguments.isEmpty()){
                emptyMap()
            }else{
                it.arguments.map { it.toKeyedArguments() }.flatten()
            }
            it to  argumentContext.copy(argumentSubstitutes = (argumentContext.argumentSubstitutes + funArgs).toMutableMap())
        }.map {
            async { CommandCompiler(it.first.usage, it.second, it.first, services =services).invoke() }
        }.map { it.await() }



        builder.await().build().also {client ->
            listeners.forEach {  client.dispatcher.registerListener(it) }
        }
    }

    companion object {
        internal val logger = LoggerFactory.getLogger(SetupContext::class.java)
    }
}

data class ArgumentContext internal constructor(
    var argumentToken : String = "$",
    internal val tokenSubstitutes: MutableMap<Char,Argument> = mutableMapOf(),
    internal val argumentSubstitutes: MutableMap<String,Argument> = mutableMapOf()
){
    fun forToken(token: Char, supplier: () -> Argument){
        if(tokenSubstitutes.containsKey(token)){
            throw IllegalArgumentException("token $token has already been set")
        }else{
            tokenSubstitutes[token] = supplier()
        }
    }

    fun forArgument(argument: String, supplier: () -> Argument){
        if(argumentSubstitutes.containsKey(argument)){
            throw IllegalArgumentException("argument $argument has already been set")
        }else{
            argumentSubstitutes[argument] = supplier()
        }
    }
}