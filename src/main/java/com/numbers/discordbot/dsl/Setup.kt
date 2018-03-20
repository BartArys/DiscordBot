package com.numbers.discordbot.dsl

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

    operator fun invoke(): IDiscordClient {
        val builder = ClientBuilder().withToken(token.toString()).withRecommendedShardCount()

        services = injector.build()

        val commands =  commandPackages.flatMap { findCommands(it) } + commands
        logger.info("found ${commands.size} commands")
        logger.debug("commands: {}", commands)

        val beforeConfig = System.currentTimeMillis()
        val listeners = commands.map {
            val funArgs = it.arguments.map { it.toKeyedArguments() }.flatten()
            val context =  argumentContext.copy(argumentSubstitutes = (argumentContext.argumentSubstitutes + funArgs).toMutableMap())
            CommandCompiler(it.usage, context, it, services =services).invoke()
        }

        val configTime = System.currentTimeMillis()
        logger.info("building commands took {} ms", configTime - beforeConfig)

        return builder.build().also { client ->
            listeners.forEach { client.dispatcher.registerListener(it) }
            val end = System.currentTimeMillis()
            logger.info("client building took {} ms", end - configTime)
        }
    }

    companion object {
        internal val logger by lazy { LoggerFactory.getLogger(SetupContext::class.java) }
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