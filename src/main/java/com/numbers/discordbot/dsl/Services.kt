package com.numbers.discordbot.dsl

import com.google.inject.*
import com.google.inject.Binder
import kotlinx.coroutines.experimental.runBlocking
import kotlin.reflect.KClass

interface ServicesInjector{

    val injections: MutableList<(Binder) -> Unit>
    val contextSuppliers: MutableMap<KClass<*>, suspend (CommandContext) -> Any>

    companion object Factory {
        private class InjectionModule(val injections: MutableList<(Binder) -> Unit>) : AbstractModule(){
            override fun configure() {
                injections.forEach { it.invoke(binder()) }
            }
        }

        private class InternalServicesInjector(
                override val injections: MutableList<(Binder) -> Unit> = mutableListOf(),
                override val contextSuppliers: MutableMap<KClass<*>, suspend (CommandContext) -> Any> = mutableMapOf()
        ) : ServicesInjector

        private val injector by lazy { InternalServicesInjector() }

        internal operator fun invoke() : ServicesInjector = injector
    }

    fun build() : Services  {
        val injector = Guice.createInjector(InjectionModule(injections))
        return Services(injector, contextSuppliers)
    }

}

inline fun<reified T, E> ServicesInjector.inject(singleton: E) where E : T = injections.add { it.bind(T::class.java).toInstance(singleton) }

inline fun <reified E, reified T> ServicesInjector.injectSupplier() where T : E = injections.add { it.bind(E::class.java).to(T::class.java) }

inline fun <reified E> ServicesInjector.injectSupplier(supplier: Provider<out E>) = injections.add { it.bind(E::class.java).toProvider(supplier) }

inline fun <reified T> ServicesInjector.injectContextually(noinline unit: suspend (CommandContext) -> T) where T : Any{
    if(contextSuppliers.containsKey(T::class)){
        throw IllegalArgumentException("")
    }
    contextSuppliers[T::class] = unit
}


open class Services internal constructor(
        val injector: Injector,
        val contextSuppliers: MutableMap<KClass<*>, suspend (CommandContext) -> Any>,
        var context: CommandContext? = null
){

    inline operator fun <reified T> invoke() : T {
        context?.let {
            contextSuppliers[T::class]?.let { supplier -> return runBlocking { supplier.invoke(this@Services.context!!) } as T }
        }
        return injector.getInstance(T::class.java)!!
    }

}
