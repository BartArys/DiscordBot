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
        handle(event.getGuild(), cache);
    }
    
    @Command
    @MessageFilter(eventType = MessageEvent.class, startsWith = "leave", prefixCheck = true)
    public void handle(MessageEvent event, MusicManagerCache cache)
    {
        handle(event.getGuild(), cache);
    }
    
    public void handle(IGuild guild, MusicManagerCache cache)
    {
        IVoiceChannel channel = guild.getConnectedVoiceChannel();
        if(channel != null){
            GuildMusicManager gmm = cache.getGuildMusicManager(guild);
            gmm.scheduler.clear();
            gmm.player.stopTrack();
            channel.leave();
        }
        
    }
    
    
    
}
