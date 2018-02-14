package com.numbers.discordbot.service

import com.mongodb.async.client.MongoCollection
import org.bson.conversions.Bson
import kotlin.coroutines.experimental.suspendCoroutine

abstract class AbstractDOptionalBService<T,E,in F>(private val filter: (F) -> Bson, private val mapper: (T) -> E, private val reverseMapper: (F, E) -> T) {

    protected abstract val collection : MongoCollection<T>

    suspend fun get(`for` : F) : E? = suspendCoroutine { cont ->
        collection.find(filter(`for`)).first { result, t ->
            t?.let { cont.resumeWithException(it); return@first }

            val value = result?.let { mapper(it) }
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