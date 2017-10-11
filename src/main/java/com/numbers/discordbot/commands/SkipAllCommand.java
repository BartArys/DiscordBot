package com.numbers.discordbot.commands;

import com.numbers.discordbot.Command;
import com.numbers.discordbot.audio.GuildMusicManager;
import com.numbers.discordbot.audio.MusicManagerCache;
import com.numbers.discordbot.filter.MessageFilter;
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent;
import sx.blah.discord.handle.impl.obj.Embed;
import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Command(name =  "Skip All Songs")
public class SkipAllCommand {

    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true, regex = ".*skipall.*", readableUsage = "skipall")
    public void handle(MentionEvent event, MusicManagerCache cache, ScheduledExecutorService ses){
    
        GuildMusicManager gmm = cache.getGuildMusicManager(event.getGuild());
        EmbedBuilder builder = new EmbedBuilder().withColor(Color.BLUE);
        
        if(gmm.player.getPlayingTrack() == null && gmm.scheduler.isEmpty()){
            IEmbed.IEmbedField noSongsField = new Embed.EmbedField("Skipped",
                "No songs to skip.", false);
            
            builder.appendField(noSongsField);
        }else{
            long skippedAmount = gmm.scheduler.getSize();
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
