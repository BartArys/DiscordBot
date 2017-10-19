package com.numbers.discordbot.commands;

import com.numbers.discordbot.Command;
import com.numbers.discordbot.audio.GuildMusicManager;
import com.numbers.discordbot.audio.MusicManagerCache;
import com.numbers.discordbot.filter.MessageFilter;
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageTokenizer;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Command(name =  "Skip Songs")
public class SkipCommand {

    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true,
            regex = ".*skip\\s\\d+", readableUsage = "skip {$numberOfSongs}")
    public void handle(MentionEvent event, MusicManagerCache cache,
                       ScheduledExecutorService ses)
    {
        MessageTokenizer tokenizer = new MessageTokenizer(event.getMessage());
        tokenizer.nextMention(); //bot
        tokenizer.nextWord(); //skip
        long skipAmount;
        if(tokenizer.hasNextWord()){
            skipAmount = Long.parseLong(tokenizer.nextWord().getContent());
        }else {
            skipAmount = 1;
        }
        skipSongs(skipAmount, event, cache, ses);
    }

    @Command
    @MessageFilter(eventType = MessageEvent.class, prefixCheck = true, regex = "skip\\s\\d+", readableUsage = "skip {$numberOfSong}")
    public void handle(MessageEvent event, MusicManagerCache cache, ScheduledExecutorService ses){
        MessageTokenizer tokenizer = new MessageTokenizer(event.getMessage());
        tokenizer.nextWord(); //prefix
        tokenizer.nextWord(); //skip
        long skipAmount;
        if(tokenizer.hasNextWord()){
            skipAmount = Long.parseLong(tokenizer.nextWord().getContent());
        }else {
            skipAmount = 1;
        }
        skipSongs(skipAmount, event, cache, ses);
    }

    private void skipSongs(long skipAmount, MessageEvent event, MusicManagerCache cache, ScheduledExecutorService ses){

        GuildMusicManager gmm = cache.getGuildMusicManager(event.getGuild());

        if (gmm.player.getPlayingTrack() != null) {
            gmm.scheduler.remove(skipAmount - 1);
            gmm.player.stopTrack();
        } else {
            gmm.scheduler.remove(skipAmount);
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(Color.BLUE);

        if (gmm.scheduler.getSize() != 0) {
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
