package com.numbers.discordbot.module.music

import com.numbers.disko.MemoryStore
import com.numbers.disko.Store
import com.numbers.disko.discord.DiscordMessage

object MusicPlayerMessageStore : Store<DiscordMessage> by MemoryStore()