package com.numbers.discordbot.dsl.guard

import com.numbers.discordbot.dsl.CommandContext
import com.numbers.discordbot.dsl.discord.DiscordMessage
import com.numbers.discordbot.extensions.isFull
import sx.blah.discord.api.events.Event
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.handle.obj.*

inline fun<reified T> T.guard(condition : T.() -> Boolean, block: T.() -> Unit) where T : IDiscordObject<*> {
    if(condition(this)) block()
}

inline fun<reified T> T.guard(condition : T.() -> Boolean, block: T.() -> Unit) where T : DiscordMessage{
    if(condition(this)) block()
}

inline fun<reified T> T.guard(condition : T.() -> Boolean, block: T.() -> Unit) where T : Event{
    if(condition(this)) block()
}

inline fun CommandContext.guard(condition : CommandContext.() -> Boolean, block: CommandContext.() -> Unit){
    if(condition(this)) block()
}

inline val ReactionAddEvent.isAuthor : Boolean get() { return user.longID == author.longID }

fun ReactionAddEvent.byUser(user: IUser) : Boolean { return user.longID == author.longID }

inline val IChannel.canRemoveEmoji: Boolean get() = getModifiedPermissions(client.ourUser).contains(Permissions.MANAGE_MESSAGES)
inline val IChannel.canReact: Boolean get() = getModifiedPermissions(client.ourUser).contains(Permissions.MANAGE_MESSAGES)
inline val IChannel.canMessage: Boolean get() = getModifiedPermissions(client.ourUser).contains(Permissions.SEND_MESSAGES)
inline val IChannel.canSendFiles : Boolean get() = getModifiedPermissions(client.ourUser).contains(Permissions.ATTACH_FILES)
inline val IChannel.canDeleteMessage : Boolean get() = getModifiedPermissions(client.ourUser).contains(Permissions.MANAGE_MESSAGES)
inline val IVoiceChannel.canJoin : Boolean get() = getModifiedPermissions(client.ourUser).contains(Permissions.VOICE_CONNECT) && !this.isFull
inline val IVoiceChannel.canSpeak : Boolean get() = getModifiedPermissions(client.ourUser).contains(Permissions.VOICE_CONNECT)
inline val IVoiceChannel.canMove : Boolean get() = getModifiedPermissions(client.ourUser).contains(Permissions.VOICE_MOVE_MEMBERS)
inline val CommandContext.canReact: Boolean get() = this.channel.canReact
inline val CommandContext.canSendMessage: Boolean get() = this.channel.canMessage
inline val CommandContext.canSendFiles : Boolean get() = this.channel.canSendFiles
inline val CommandContext.canDeleteMessage: Boolean get() = this.channel.canDeleteMessage
