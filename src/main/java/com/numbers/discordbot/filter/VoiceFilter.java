package com.numbers.discordbot.filter;

import java.lang.annotation.*;
import sx.blah.discord.handle.impl.events.guild.voice.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface VoiceFilter {

    Class<? extends VoiceChannelEvent> eventType();
    
}
