package com.numbers.discordbot.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import sx.blah.discord.handle.obj.IUser

data class PermissionsList(var objectId: ObjectId? = null, var userId: String? = null, var permissions: List<String>? = null)

@Singleton
class PermissionsService @Inject constructor(db: MongoDatabase)
    : AbstractDBService<PermissionsList, List<Permission>, IUser>(
        filter =  { Filters.eq<String>("userId", it.stringID) },
        reverseMapper = { user, list -> PermissionsList(null, user.stringID, list.map { it.name }.toMutableList()) },
        default = PermissionsList(null, null, mutableListOf()),
        mapper = { it.permissions?.map { Permission.valueOf(it) }?.toMutableList() ?: mutableListOf() }
) {
    override val collection: MongoCollection<PermissionsList> = db.getCollection("permissions", PermissionsList::class.java)
}

enum class Permission(val value : Long, val permissionLevel: Int){
    ADMIN   (0x999999999999999, Int.MAX_VALUE),
    CLAP    (0x000000000000001, Int.MIN_VALUE),
    TAG     (0x000000000000002, Int.MIN_VALUE),
    PERSONA (0x000000000000004, Int.MIN_VALUE),
    PLAYLIST(0x000000000000008, Int.MIN_VALUE),
    MUSIC   (0x000000000000010, Int.MIN_VALUE),
    USER    (0x000000000000020, Int.MIN_VALUE),
    MANAGE  (0x000000000000040, 1);
}

fun Iterable<Permission>.hasPermission(permission: Permission) : Boolean = any { it.value and permission.value == it.value }

fun Iterable<Permission>.mayAdd(permission: Permission) : Boolean = any { it.value >= permission.value }

operator fun Iterable<Permission>.compareTo(that : Iterable<Permission>) : Int{
    val thisMax = this.maxBy { it.permissionLevel }?.permissionLevel ?: Int.MIN_VALUE
    val thatMax = that.maxBy { it.permissionLevel }?.permissionLevel ?: Int.MIN_VALUE

    return thisMax.compareTo(thatMax)
}
