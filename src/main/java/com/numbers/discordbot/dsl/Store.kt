package com.numbers.discordbot.dsl

interface Store<T>{

    fun<T> getEntity(clazz: Class<T>, key: Long) : T? where T: Any
    fun storeEntity(key: Long, entity: Any) : Boolean
    fun removeEntity(key: Long) : Boolean
    fun removeEntity(key: Long, entity: Any) : Boolean
}

inline fun<T> Store<T>.getOrCreateEntity(clazz: Class<T>, key: Long, supplier: () -> T) : T where T: Any = getEntity(clazz, key) ?: supplier().also { storeEntity(key, it) }

inline operator fun<reified T> Store<T>.invoke(key: Long) : T? where T: Any = getEntity(T::class.java, key)
inline operator fun<reified T> Store<T>.invoke(key: Long, supplier: () -> T) : T? where T: Any
        = getOrCreateEntity(T::class.java, key, supplier)

class MemoryStore<T> : Store<T>{
    private val cache = mutableMapOf<Long, Any>()

    override fun <T> getEntity(clazz: Class<T>, key: Long): T? where T: Any = cache[key]?.let {  it as? T }

    override fun storeEntity(key: Long, entity: Any): Boolean = cache.putIfAbsent(key, entity) == null

    override fun removeEntity(key: Long): Boolean = cache.remove(key) != null

    override fun removeEntity(key: Long, entity: Any): Boolean = cache.remove(key, entity)
}

