package com.numbers.discordbot.commands;

import com.numbers.discordbot.Command;
import com.numbers.discordbot.audio.GuildMusicManager;
import com.numbers.discordbot.audio.MusicManagerCache;
import com.numbers.discordbot.filter.MessageFilter;
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Command(name =  "Pause Music")
public class PauseCommand {

    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true,
                   regex = ".*pause", readableUsage = "pause")
    public void handle(MentionEvent event, MusicManagerCache cache,
                       ScheduledExecutorService ses)
    {
        handleCommand(event, cache, ses);
    }

    @Command
    @MessageFilter(eventType = MessageEvent.class, prefixCheck = true, regex = "pause", readableUsage = "pause")
    public void handlePrefix(MessageEvent event, MusicManagerCache cache, ScheduledExecutorService ses)
    {
        handleCommand(event, cache, ses);
    }
    
    private void handleCommand(MessageEvent event, MusicManagerCache cache,
                       ScheduledExecutorService ses)
    {

        GuildMusicManager gmm = cache.getGuildMusicManager(event.getGuild());

        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(Color.BLUE);

        if (gmm.player.getPlayingTrack() == null) {
            builder.appendDesc("no song in queue");
        } else if (gmm.player.isPaused()) {
            String nowPlaying = String.format(
                    "already paused: [%s][%s] [%s](%s)",
                    LocalTime.MIN.plus(gmm.player.getPlayingTrack()
                            .getPosition(), ChronoUnit.MILLIS).withNano(0)
                    .format(
                            DateTimeFormatter.ISO_TIME),
                    LocalTime.MIN.plus(gmm.player.getPlayingTrack()
                            .getDuration(), ChronoUnit.MILLIS).withNano(0)
                    .format(
                            DateTimeFormatter.ISO_TIME),
                    gmm.player.getPlayingTrack().getInfo().title,
                    gmm.player.getPlayingTrack().getInfo().uri);
            builder.appendDesc(nowPlaying);
        } else {
            gmm.player.setPaused(true);
            String alreadyPlaying = String.format(
                    "paused: [%s][%s] [%s](%s)",
                    LocalTime.MIN.plus(gmm.player.getPlayingTrack()
                            .getPosition(), ChronoUnit.MILLIS).withNano(0)
                    .format(
                            DateTimeFormatter.ISO_TIME),
                    LocalTime.MIN.plus(gmm.player.getPlayingTrack()
                            .getDuration(), ChronoUnit.MILLIS).withNano(0)
                    .format(
                            DateTimeFormatter.ISO_TIME),
                    gmm.player.getPlayingTrack().getInfo().title,
                    gmm.player.getPlayingTrack().getInfo().uri);
            builder.appendDesc(alreadyPlaying);
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
