package com.numbers.discordbot.dsl.permission

import sx.blah.discord.handle.obj.IUser
import java.util.*

interface Permission{
    val name: String
}

interface PermissionSupplier{
    fun forUser(user: IUser) : Iterable<Permission>
}

object NoOpPermissionSupplier : PermissionSupplier{
    override fun forUser(user: IUser): Iterable<Permission> = Collections.emptyList()
}
