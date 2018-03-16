package com.numbers.discordbot.dsl

import kotlin.reflect.KClass

abstract class Store{

    abstract fun<T> getEntity(clazz: Class<T>, key: String) : T? where T: Any
    abstract fun<T> getOrCreateEntity(clazz: Class<T>, key: String, supplier: () -> T) : T where T: Any
    abstract fun storeEntity(key: String, entity: Any) : Boolean
    abstract fun removeEntity(key: String) : Boolean
    abstract fun removeEntity(key: String, entity: Any) : Boolean

    inline operator fun<reified T> invoke(key: String) : T? where T: Any = getEntity(T::class.java, key)
    inline operator fun<reified T> invoke(key: String, noinline supplier: () -> T) : T? where T: Any
            = getOrCreateEntity(T::class.java, key, supplier)
}

val MemoryStore = object : Store(){
    internal val cache = mutableMapOf<String, Any>()

    override fun <T> getEntity(clazz: Class<T>, key: String): T? where T: Any = cache[key]?.let {  it as? T }

    override fun <T> getOrCreateEntity(clazz: Class<T>, key: String, supplier: () -> T): T where T: Any
            = getEntity(clazz, key) ?: supplier().also { storeEntity(key, it) }

    override fun storeEntity(key: String, entity: Any): Boolean = cache.putIfAbsent(key, entity) == null

    override fun removeEntity(key: String): Boolean = cache.remove(key) != null

    override fun removeEntity(key: String, entity: Any): Boolean = cache.remove(key, entity)
}

fun Any.storeInMemory(key: String) = MemoryStore.storeEntity(key, this)
fun<T> KClass<T>.getFromMemoryStore(key: String) where T: Any = MemoryStore.getEntity(this.java, key)

