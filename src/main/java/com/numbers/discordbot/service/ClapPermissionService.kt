package com.numbers.discordbot.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import sx.blah.discord.handle.obj.IUser
import kotlin.coroutines.experimental.suspendCoroutine

data class ClapPermission(val id: ObjectId? = null, var userId: String? = null, var allowed: Boolean? = null)

@Singleton
class ClapPermissionService  @Inject constructor(db: MongoDatabase) {

    companion object {
        val default = false
    }

    private val permissions: MongoCollection<ClapPermission> = db.getCollection("clapPermissions", ClapPermission::class.java)

     suspend fun mayClap(forUser: IUser) : Boolean = suspendCoroutine {  cont ->
        permissions.find(Filters.eq<String>("userId", forUser.stringID)).first{ permission: ClapPermission?, t: Throwable? ->
            t?.let {
                cont.resumeWithException(t)
                return@first
            }

            cont.resume(permission?.allowed ?: default)
        }
    }

    fun setPermission(forUser: IUser, allowed: Boolean) {
        permissions.find(Filters.eq<String>("userId", forUser.stringID)).first {
            permission: ClapPermission?, _ ->

            if(permission != null){
                permissions.replaceOne(Filters.eq<String>("userId", forUser.stringID), permission.copy(allowed = allowed)) {_,_ -> }
            }else{
                permissions.insertOne(ClapPermission(userId = forUser.stringID, allowed = allowed)) {_,_ -> }
            }
        }
    }

}
