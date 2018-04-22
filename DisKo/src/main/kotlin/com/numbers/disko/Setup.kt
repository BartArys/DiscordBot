package com.numbers.disko

import com.numbers.disko.permission.NoOpPermissionSupplier
import com.numbers.disko.permission.PermissionSupplier
import org.slf4j.LoggerFactory
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

/**
 * Configures the [SetupContext].
 *
 * The [SetupContext] contains all the setup needed for most users to configure
 * their commands and its dependencies.
 *
 * @author Bart Arys
 * @param setup the configuration of the [SetupContext]
 * @return the @SetupContext as configured
 */
inline fun setup(baseContext: SetupContext = SetupContext.sharedContext, setup: SetupContext.() -> Unit): SetupContext {
    baseContext.apply(setup)
    return baseContext
}

/**
 * Builder class for configuring the behaviour of the bot
 * @author Bart Arys
 */
data class SetupContext internal constructor(
        val injector: ServicesInjector,
        val argumentContext: ArgumentContext,
        val commands: MutableList<Command> = mutableListOf(),
        val commandPackages: MutableList<String> = mutableListOf(),
        var token: CharSequence?,
        var supplier: PermissionSupplier
) {
    val services: Services by lazy { injector.build() }

    /**
     * Add the given dependency injections to the [SetupContext], these can later
     * be used in the [Services] of a [Command.execute]
     *
     * @param injections the dependency injections to add to the bot
     */
    inline fun inject(injections: ServicesInjector.() -> Unit) = injector.injections()

    /**
     * Add global arguments to the bot, these will apply to every [Command]
     *
     * @param arguments the arguments to add to the bot
     */
    inline fun arguments(arguments: ArgumentContext.() -> Unit) = argumentContext.arguments()

    /**
     * takes all the commands from a [CommandsContainer] and adds them to the [SetupContext]
     *
     * These [Commands][Command] will be registered to the bot during the [SetupContext.invoke]
     *
     * @param container the container from which its commands will be taken
     */
    infix operator fun plus(container: CommandsContainer) {
        commands.addAll(container.commands)
    }

    /**
     * merge another [SetupContext] with this one, its  [ArgumentContext] and [Command] list will be
     *
     * @argument sharedContext the [SetupContext] to merge with
     */
    infix operator fun plusAssign(setup: SetupContext) {
        argumentContext.argumentSubstitutes.putAll(setup.argumentContext.argumentSubstitutes)
        argumentContext.tokenSubstitutes.putAll(setup.argumentContext.tokenSubstitutes)
        commands.addAll(setup.commands)
    }

    /**
     * Builds an [IListener] from the given [Command] based on the @SetupContext
     *
     * &nbsp;
     *
     * Note that this Command will not be added to the bot by default.
     *
     * It is advised the method is only called for generating a [Command] after the initial setup, for example a [Command] that wants to add another [Command].
     *
     * @argument command the [Command] to compile
     * @return an [IListener]  represented by the given [Command]
     */
    fun compile(command: Command): IListener<MessageReceivedEvent> {
        val commandArguments = mutableMapOf<String, Argument>()
        command.arguments.map { it.toKeyedArguments() }.forEach { commandArguments.putAll(it) }
        val context = argumentContext.copy(argumentSubstitutes = (argumentContext.argumentSubstitutes + commandArguments).toMutableMap())
        return CommandCompiler(command.usage, context, command, services = services, supplier = supplier).invoke()
    }

    /**
     * alias of [SetupContext.invoke]
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun build(): IDiscordClient = invoke()

    /**
     *  Builds the bot based on the previously given setup.
     *z
     *  &nbsp;
     *
     *  Note that the given bot has only been created and is not logged in yet.
     *
     *  @return a [IDiscordClient] configured by this context
     */
    operator fun invoke(): IDiscordClient {
        if (token.isNullOrEmpty()) throw IllegalStateException("a Discord token must have been supplied before calling invoke")
        val builder = ClientBuilder().withToken(token.toString()).withRecommendedShardCount()

        val commands = commandPackages.flatMap { findCommands(it) } + commands
        logger.info("found {} commands", commands.size)
        if(logger.isDebugEnabled){
            commands.forEach {
                logger.debug("found: {}", it)
            }
        }
        commands.filter { it.info.isEmpty() }.forEach {
            logger.debug("command ${it.usage} is missing info, you should probably fill this in for it to be usable in the help function")
        }
        val listeners = logger.measureIfDebug("building commands") {
            commands.map {
                val commandArguments = it.arguments.map { it.toKeyedArguments() }.flatMap { it.entries.map { it.key to it.value } }.toMap()
                val context = argumentContext.copy(argumentSubstitutes = (argumentContext.argumentSubstitutes + commandArguments).toMutableMap())
                CommandCompiler(it.usage, context, it, services = services, supplier = supplier).invoke()
            }
        }

        return logger.measureIfDebug("client building") {
            listeners.forEach { builder.registerListener(it) }
            builder.build()
        }
    }

    companion object {
        internal val logger by lazy { LoggerFactory.getLogger(SetupContext::class.java) }

        val sharedContext
                by lazy { SetupContext(ServicesInjector(), ArgumentContext(), token = "", supplier = NoOpPermissionSupplier) }
    }

}

data class ArgumentContext internal constructor(
        var argumentToken: String = "$",
        val tokenSubstitutes: MutableMap<Char, Argument> = mutableMapOf(),
        val argumentSubstitutes: MutableMap<String, Argument> = mutableMapOf()
) {
    fun forToken(token: Char, supplier: Argument) {
        if (tokenSubstitutes.containsKey(token)) {
            throw IllegalArgumentException("token $token has already been set")
        } else {
            tokenSubstitutes[token] = supplier
        }
    }

    fun forArgument(argument: String, supplier: Argument) {
        if (argumentSubstitutes.containsKey(argument)) {
            throw IllegalArgumentException("argument $argument has already been set")
        } else {
            argumentSubstitutes[argument] = supplier
        }
    }
}