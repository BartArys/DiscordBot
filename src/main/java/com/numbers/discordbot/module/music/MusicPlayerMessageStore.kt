package com.numbers.discordbot.module.music

import com.numbers.discordbot.dsl.MemoryStore
import com.numbers.discordbot.dsl.Store
import com.numbers.discordbot.dsl.discord.DiscordMessage

object MusicPlayerMessageStore : Store<DiscordMessage> by MemoryStore()