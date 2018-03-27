package com.numbers.discordbot.extensions

import sx.blah.discord.handle.obj.IVoiceChannel

inline val IVoiceChannel.isFull : Boolean  get() = userLimit > 0 && connectedUsers.count() >= userLimit
