package com.numbers.discordbot.commands;

import com.numbers.discordbot.Command;
import com.numbers.discordbot.audio.GuildMusicManager;
import com.numbers.discordbot.audio.MusicManagerCache;
import com.numbers.discordbot.filter.MessageFilter;
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.obj.Embed;
import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Command(name =  "Print Queue")
public class ShowQueueCommand {

    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true,
            regex = ".*queue.*", readableUsage = "queue")
    public void handle(MentionEvent event, MusicManagerCache cache,
                       ScheduledExecutorService ses)
    {
        handleCommand(event, cache, ses);
    }
    
    @Command
    @MessageFilter(eventType = MessageEvent.class, prefixCheck = true, regex = "queue", readableUsage = "queue")
    public void handlePrefix(MessageEvent event, MusicManagerCache cache, ScheduledExecutorService ses){
        handleCommand(event, cache, ses);
    }
    
    public void handleCommand(MessageEvent event, MusicManagerCache cache,
                       ScheduledExecutorService ses){
        GuildMusicManager gmm = cache.getGuildMusicManager(event.getGuild());

        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(Color.BLUE);

        builder.withAuthorName("audio provided by lavaplayer");
        builder.withAuthorUrl("https://github.com/sedmelluq/lavaplayer");

        String nowPlaying;
        AtomicInteger position = new AtomicInteger();

        if (gmm.player.getPlayingTrack() != null) {
            nowPlaying = String.format("%02d: [%s][%s] %s [%s](%s)",
                    position.getAndIncrement(),
                    LocalTime.MIN.plus(gmm.player.getPlayingTrack()
                            .getPosition(), ChronoUnit.MILLIS).withNano(0)
                    .format(
                            DateTimeFormatter.ISO_TIME),
                    LocalTime.MIN.plus(gmm.player.getPlayingTrack()
                            .getDuration(), ChronoUnit.MILLIS).withNano(0)
                    .format(
                            DateTimeFormatter.ISO_TIME),
                    gmm.player.isPaused()
                    ? "PAUSED"
                    : "",
                    gmm.player.getPlayingTrack().getInfo().title,
                    gmm.player.getPlayingTrack().getInfo().uri);
        } else {
            nowPlaying = "nothing";
        }

        IEmbed.IEmbedField nowPlayingField = new Embed.EmbedField("Now playing",
                nowPlaying, false);
        builder.appendField(nowPlayingField);

        String queue = gmm.scheduler.getQueueStream().limit(5).map(track
                -> String.format("%02d: [%s] [%s](%s)",
                        position.getAndIncrement(),
                        LocalTime.MIN.plus(track.getDuration(),
                                ChronoUnit.MILLIS).withNano(0).format(
                        DateTimeFormatter.ISO_TIME),
                        track.getInfo().title,
                        track.getInfo().uri)
        ).collect(Collectors.joining("\n"));

        if (!queue.isEmpty()) {
            IEmbed.IEmbedField queueField = new Embed.EmbedField("Queue", queue,
                    false);

            builder.appendField(queueField);
        }

        if (position.get() >= 5) { // more songs
            IEmbed.IEmbedField remaining = new Embed.EmbedField("Remaining",
                    String.format("%s remaining", gmm.scheduler.getQueueStream()
                            .count() - 11), false);

            builder.appendField(remaining);
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
