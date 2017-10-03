package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import com.numbers.discordbot.audio.*;
import com.numbers.discordbot.filter.*;
import java.awt.*;
import java.time.*;
import java.util.concurrent.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

@Command
public class SkipAllCommand {

    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true, regex = ".*skipall.*")
    public void handle(MentionEvent event, MusicManagerCache cache, ScheduledExecutorService ses){
    
        GuildMusicManager gmm = cache.getGuildMusicManager(event.getGuild());
        EmbedBuilder builder = new EmbedBuilder().withColor(Color.BLUE);
        
        if(gmm.player.getPlayingTrack() == null && gmm.scheduler.isQueueEmpty()){
            IEmbed.IEmbedField noSongsField = new Embed.EmbedField("Skipped",
                "No songs to skip.", false);
            
            builder.appendField(noSongsField);
        }else{
            long skippedAmount = gmm.scheduler.getQueueSize();
            gmm.scheduler.clear();
            
            if(gmm.player.getPlayingTrack() != null){
                skippedAmount++;
                gmm.player.stopTrack();
            }
            
            IEmbed.IEmbedField skippedSongsField = new Embed.EmbedField("Skipped",
                String.format("Skipped %d songs", skippedAmount), false);
            
            builder.appendField(skippedSongsField);
        }
        
        builder.withFooterText("this message will be deleted in 10 seconds");
        builder.withTimestamp(LocalDateTime.now());

        IMessage message = event.getChannel().sendMessage(builder.build());
        ses.schedule(() -> {
            message.delete();
            event.getMessage().delete();
        }, 10, TimeUnit.SECONDS);
        
    }
    
}
