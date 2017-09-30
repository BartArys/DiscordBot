package com.numbers.discordbot.commands;

import com.numbers.discordbot.audio.*;
import com.numbers.discordbot.filter.*;
import java.util.*;
import sx.blah.discord.api.events.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.handle.obj.*;

public class LeaveVoiceCommand implements IListener<MentionEvent>{

    @Override
    @Filter(eventType = MentionEvent.class, regex = ".*leave.*", mentionsBot = true)
    public void handle(MentionEvent event)
    {
        IVoiceChannel connected = event.getGuild().getConnectedVoiceChannel();
        if(connected != null){
            Audio.getGuildAudioPlayer(event.getGuild()).player.destroy();
            connected.leave();
        }
        
    }

    
    
}
