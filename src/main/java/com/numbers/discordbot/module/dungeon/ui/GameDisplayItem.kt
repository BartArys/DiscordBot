package com.numbers.discordbot.module.dungeon.ui

import sx.blah.discord.handle.impl.obj.Embed

interface GameDisplayItem {

    val name : String

    val shortcuts: Iterable<ShortcutEmote>

    fun drawDisplay() : Iterable<Embed.EmbedField>

}