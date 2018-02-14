package com.numbers.discordbot.dsl

import com.google.inject.*
import com.google.inject.Binder
import kotlin.reflect.KClass

class ServicesInjector internal constructor(){

    val injections: MutableList<(Binder) -> Unit> = mutableListOf()
    val contextSuppliers: MutableMap<KClass<*>, suspend (CommandContext) -> Any> = mutableMapOf()

    inline fun<reified T, E> inject(singleton: E) where E : T = injections.add { it.bind(T::class.java).toInstance(singleton) }

    inline fun <reified E, reified T> injectSupplier() where T : E = injections.add { it.bind(E::class.java).to(T::class.java) }

    inline fun <reified E> injectSupplier(supplier: Provider<out E>) = injections.add { it.bind(E::class.java).toProvider(supplier) }

    inline fun <reified T> injectContextually(noinline unit: suspend (CommandContext) -> T) where T : Any{
        if(contextSuppliers.containsKey(T::class)){
            throw IllegalArgumentException("")
        }
        contextSuppliers[T::class] = unit
    }

    fun build() : Services  {
        val injector = Guice.createInjector(object : AbstractModule(){
            override fun configure() {
                injections.forEach { it.invoke(binder()) }
            }
        })

        return Services(injector, contextSuppliers)
    }

}

class Services internal constructor(
        val injector: Injector,
        val contextSuppliers: MutableMap<KClass<*>, suspend (CommandContext) -> Any>,
        var context: CommandContext? = null
){

    suspend inline operator fun <reified T> invoke() : T {
        context?.let {
            contextSuppliers[T::class]?.let { return it.invoke(context!!) as T }
        }
        return injector.getInstance(T::class.java)!!
    }

}
