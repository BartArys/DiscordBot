package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import com.numbers.discordbot.filter.*;
import java.awt.*;
import sx.blah.discord.handle.impl.events.guild.voice.user.*;
import sx.blah.discord.util.*;

@VoiceCommand
public class LonelyCommand {

    @VoiceCommand
    @VoiceFilter(eventType = UserSpeakingEvent.class)
    public void handle(UserSpeakingEvent event)
    {

        if (event.getVoiceChannel().getConnectedUsers().size() == 2
                && event.getVoiceChannel().isConnected()) {
            if (!event.isSpeaking()) {
                event.getUser().getOrCreatePMChannel().sendMessage(
                        new EmbedBuilder().withColor(Color.BLUE)
                        .withDesc(
                                "While I can execute many features, a voice enabled chatbot is not something i can do :(")
                        .build());
            }
        }

    }

}
