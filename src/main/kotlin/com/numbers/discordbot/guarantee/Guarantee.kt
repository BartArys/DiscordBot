package com.numbers.discordbot.guarantee

import sx.blah.discord.handle.obj.Permissions

annotation class Guarantee(vararg val guarantees: Guarantees)

annotation class Permission(vararg val permissions: Permissions)

enum class Guarantees{
    VOICE_CHANNEL_JOINED,
}