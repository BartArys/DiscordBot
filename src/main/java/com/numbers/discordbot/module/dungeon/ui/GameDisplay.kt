package com.numbers.discordbot.module.dungeon.ui

import com.numbers.discordbot.extensions.then
import com.vdurmont.emoji.EmojiManager
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuilder

interface GameDisplay{

    val items : MutableMap<String, GameDisplayItem>

    val message : IMessage

    @EventSubscriber
    fun onReaction(event: ReactionAddEvent){
        val item = items.map { it.value }
                .firstOrNull { it.shortcuts.any { it.unicode == event.reaction.emoji.name } }

        item?.drawDisplay()?.let {
            val builder = EmbedBuilder()
            it.forEach { builder.appendField(it) }
            val requestBuilder = RequestBuilder(event.client).shouldBufferRequests(true)

            requestBuilder.doAction { message.edit(builder.build()); true }

            requestBuilder.then { message.removeReaction(event.author, event.reaction) }


            val keys = items.flatMap { it.value.shortcuts }
            val oldKeys = message.reactions.map { it.emoji.name }

            val keysToAdd = keys.filter { !oldKeys.contains(it.unicode) }
            val keysToRemove = oldKeys.filter { !keys.map { it.unicode }.contains(it) }

            keysToRemove.forEach {
                requestBuilder.then { message.removeReaction(event.client.ourUser, it) }
            }

            keysToAdd.forEach {
                requestBuilder.then { message.addReaction(EmojiManager.getByUnicode(it.unicode)) }
            }

            requestBuilder.build()
        }
    }




}