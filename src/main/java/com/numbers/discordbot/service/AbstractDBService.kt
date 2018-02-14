package com.numbers.discordbot.service

import com.mongodb.async.client.MongoCollection
import org.bson.conversions.Bson
import kotlin.coroutines.experimental.suspendCoroutine

abstract class AbstractDBService<T,E,in F>(private val default: T, private val filter: (F) -> Bson, private val mapper: (T) -> E, private val reverseMapper: (F,E) -> T) {

    protected abstract val collection : MongoCollection<T>

    suspend fun get(`for` : F) : E = suspendCoroutine { cont ->
        collection.find(filter(`for`)).first { result, t ->
            t?.let { cont.resumeWithException(it); return@first }

            val value = mapper((result ?: default))
            cont.resume(value)
        }
    }

    fun set(`for`: F, value: E){
        collection.find(filter(`for`)).first { result, _ ->
            if(result == null) collection.insertOne(reverseMapper(`for`, value)) { _,_ -> }
            else collection.replaceOne(filter(`for`), reverseMapper(`for`, value)) { _,_ -> }
        }
    }

    fun remove(`for`: F){
        collection.deleteOne(filter(`for`), { _, _ ->  })
    }

}