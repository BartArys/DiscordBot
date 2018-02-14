package com.numbers.discordbot.dsl

import kotlinx.coroutines.experimental.async
import sx.blah.discord.api.ClientBuilder

fun setup(setup: SetupContext.() -> Unit) : SetupContext {
    val context = SetupContext(ServicesInjector(), ArgumentContext())
    setup.invoke(context)
    return context
}


data class SetupContext internal constructor(
    internal val injector: ServicesInjector,
    internal val argumentContext: ArgumentContext
){
    val commandPackages : MutableList<String> = mutableListOf()

    var token : CharSequence = ""

    fun inject(injections: ServicesInjector.() -> Unit) {
        injector.injections()
    }

    fun arguments(arguments : ArgumentContext.() -> Unit){
        argumentContext.arguments()
    }


    operator fun invoke() = async {
        val builder = async {
            ClientBuilder() .withToken(token.toString()).withRecommendedShardCount()
        }

        val services = injector.build()

        val commands =  commandPackages.flatMap { findCommands(it) }
        commands.forEach { println(it.usage) }

        val listeners = commands.map {

            val funArgs = if(it.arguments.isEmpty()){
                emptyMap()
            } else{
                it.arguments.map { it.toKeyedArguments() }.reduce { acc, mutableMap -> acc + mutableMap }
            }
            it to  argumentContext.copy(argumentSubstitutes = (argumentContext.argumentSubstitutes + funArgs).toMutableMap())
        }.map {
            async { CommandCompiler(it.first.usage, it.second, it.first, services =services).invoke() }
        }.map { it.await() }



        return@async builder.await().build().also {client ->
            listeners.forEach {  client.dispatcher.registerListener(it) }
        }
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