package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import com.numbers.discordbot.audio.*;
import com.numbers.discordbot.filter.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.handle.obj.*;

@Command
public class LeaveVoiceCommand{

    @Command
    @MessageFilter(eventType = MentionEvent.class, regex = ".*leave.*", mentionsBot = true)
    public void handle(MentionEvent event, MusicManagerCache cache)
    {
        IVoiceChannel connected = event.getGuild().getConnectedVoiceChannel();
        if(connected != null){
            cache.getGuildMusicManager(event.getGuild()).player.destroy();
            connected.leave();
        }
        
    }

    
    
}
