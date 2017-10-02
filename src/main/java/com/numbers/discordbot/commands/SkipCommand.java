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
public class SkipCommand {

    @Command
    @Filter(eventType = MentionEvent.class, mentionsBot = true,
            regex = ".*skip\\s\\d+")
    public void handle(MentionEvent event, MusicManagerCache cache,
                       ScheduledExecutorService ses)
    {
        MessageTokenizer tokenizer = new MessageTokenizer(event.getMessage());
        tokenizer.nextMention(); //bot
        tokenizer.nextWord(); //skip
        long skipAmount = Long.parseLong(tokenizer.nextWord().getContent());

        GuildMusicManager gmm = cache.getGuildMusicManager(event.getGuild());

        if (gmm.player.getPlayingTrack() != null) {
            gmm.scheduler.removeAmount(skipAmount - 1);
            gmm.player.stopTrack();
        } else {
            gmm.scheduler.removeAmount(skipAmount);
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(Color.BLUE);

        if (gmm.scheduler.getQueueSize() != 0) {
            gmm.scheduler.nextTrack();
            String nowPlaying = String.format("playing: [%s][%s] [%s](%s)",
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
        }

        builder.appendField("Skipped", String.format("Skipped %d songs",
                skipAmount), true);

        builder.withFooterText("this message will be deleted in 10 seconds");
        builder.withTimestamp(LocalDateTime.now());

        IMessage message = event.getChannel().sendMessage(builder.build());
        ses.schedule(() -> {
            message.delete();
            event.getMessage().delete();
        }, 10, TimeUnit.SECONDS);
    }

}
