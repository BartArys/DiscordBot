package com.numbers.discordbot.dsl

import eu.lestard.easydi.EasyDI
import kotlinx.coroutines.experimental.runBlocking
import kotlin.reflect.KClass

interface ServicesInjector {

    val context: EasyDI
    val contextSuppliers: MutableMap<KClass<*>, suspend (CommandContext) -> Any>

    companion object Factory {

        private class InternalServicesInjector(
                override val context: EasyDI = EasyDI(),
                override val contextSuppliers: MutableMap<KClass<*>, suspend (CommandContext) -> Any> = mutableMapOf()
        ) : ServicesInjector

        private val injector by lazy { InternalServicesInjector() }

        internal operator fun invoke(): ServicesInjector = injector
    }

    fun build(): Services {
        return Services(context, contextSuppliers)
    }

}

inline fun <reified T, E> ServicesInjector.inject(singleton: E) where E : T = context.bindInstance(T::class.java, singleton)

inline fun <reified E, reified T> ServicesInjector.injectSupplier() where T : E = context.bindInterface(E::class.java, T::class.java)

inline fun <reified E> ServicesInjector.injectSupplier(noinline supplier: () -> E) = context.bindProvider(E::class.java, supplier)

inline fun <reified T> ServicesInjector.injectContextually(noinline unit: suspend (CommandContext) -> T) where T : Any {
    if (contextSuppliers.containsKey(T::class)) {
        throw IllegalArgumentException("")
    }
    contextSuppliers[T::class] = unit
}


data class Services internal constructor(
        val injector: EasyDI,
        val contextSuppliers: MutableMap<KClass<*>, suspend (CommandContext) -> Any>,
        var context: CommandContext? = null
) {

    inline operator fun <reified T> invoke(): T {
        context?.let { context ->
            contextSuppliers[T::class]?.let { supplier -> return runBlocking { supplier.invoke(context) } as T }
        }
        return injector.getInstance(T::class.java)!!
    }

}
