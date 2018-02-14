package com.numbers.discordbot.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import sx.blah.discord.handle.obj.IUser
import kotlin.coroutines.experimental.suspendCoroutine

data class UserPrefix(var id: ObjectId? = null, var userId: String? = null, var prefix: String? = null)

@Singleton
class PrefixService @Inject constructor(db: MongoDatabase) {

    companion object {
        val default = ";b"
    }

    private val cache : MutableMap<IUser, String> = mutableMapOf()

    private val prefixes: MongoCollection<UserPrefix> = db.getCollection("prefixes", UserPrefix::class.java)

    suspend fun getPrefix(forUser: IUser) : String = suspendCoroutine {
        cont ->
        cache[forUser]?.let { cont.resume(it); return@suspendCoroutine }

        prefixes.find(Filters.eq<String>("userId", forUser.stringID)).first { result: UserPrefix?, t: Throwable? ->
            t?.let {
                cont.resumeWithException(t)
                return@first
            }

            cache[forUser] = result?.prefix ?: default
            cont.resume(result?.prefix ?: default)

        }
    }

    fun setPrefix(forUser: IUser, prefix: String) {
        prefixes.find(Filters.eq<String>("userId", forUser.stringID))
                .first { userPrefix: UserPrefix?, _ ->
                    cache[forUser] = prefix
                    if (userPrefix != null) {
                        prefixes.replaceOne(Filters.eq<String>("userId", forUser.stringID), userPrefix.copy(prefix = prefix)) {_,_ -> }
                    } else {
                        prefixes.insertOne(UserPrefix(userId = forUser.stringID, prefix = prefix)) {_,_ -> }
                    }
        }
    }

}