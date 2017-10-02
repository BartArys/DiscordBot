package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import com.numbers.discordbot.audio.*;
import com.numbers.discordbot.filter.*;
import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.concurrent.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

@Command
public class MoveToCommand {

    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true,
            regex = ".*moveTo\\s(\\d{2}:){0,2}\\d{2}")
    public void handle(MentionEvent event, MusicManagerCache cache,
                       ScheduledExecutorService ses)
    {
        MessageTokenizer tokenizer = new MessageTokenizer(event.getMessage());
        tokenizer.nextMention();
        tokenizer.nextWord();
        String time = tokenizer.nextWord().getContent();

        int millis = LocalTime.parse(time, DateTimeFormatter.ISO_TIME)
                .toSecondOfDay() * 1000;

        GuildMusicManager gmm = cache.getGuildMusicManager(event.getGuild());

        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(Color.BLUE);

        if (gmm.player.getPlayingTrack() != null) {
            if (gmm.player.getPlayingTrack().getDuration() < millis) {
                builder.appendDesc("given time would skip the song :confused:");
            } else {
                gmm.player.getPlayingTrack().setPosition(millis);
                String nowPlaying = String.format("[%s][%s] %s[%s](%s)",
                        LocalTime.MIN.plus(gmm.player.getPlayingTrack()
                                .getPosition(), ChronoUnit.MILLIS).withNano(0)
                        .format(
                                DateTimeFormatter.ISO_TIME),
                        LocalTime.MIN.plus(gmm.player.getPlayingTrack()
                                .getDuration(), ChronoUnit.MILLIS).withNano(0)
                        .format(
                                DateTimeFormatter.ISO_TIME),
                        gmm.player.isPaused()
                        ? "PAUSED "
                        : "",
                        gmm.player.getPlayingTrack().getInfo().title,
                        gmm.player.getPlayingTrack().getInfo().uri);
                builder.appendDesc(nowPlaying);
            }
        } else {
            builder.appendDesc("playlist is empty");
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
