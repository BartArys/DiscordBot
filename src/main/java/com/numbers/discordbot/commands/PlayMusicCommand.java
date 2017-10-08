package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import com.numbers.discordbot.audio.*;
import com.numbers.discordbot.filter.*;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.tools.*;
import com.sedmelluq.discord.lavaplayer.track.*;
import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

@Command
public class PlayMusicCommand {

    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true,
                   regex = ".*play\\s.+")
    public void handle(MentionEvent event, MusicManagerCache cache,
                       ScheduledExecutorService ses)
    {
        MessageTokenizer mt = new MessageTokenizer(event.getMessage());
        mt.nextMention(); //mention
        mt.nextWord(); //play

        handle(event, cache, ses, mt);
    }
    
    @Command
    @MessageFilter(eventType = MessageEvent.class, prefixCheck = true, startsWith = "play ")
    public void handlePrefix(MessageEvent event, MusicManagerCache cache,
                       ScheduledExecutorService ses)
    {
        System.err.println("MUSIC REQUEST");

        MessageTokenizer mt = new MessageTokenizer(event.getMessage());
        mt.nextWord(); //prefix
        mt.nextWord(); //play

        handle(event, cache, ses, mt);
    }
    
    public void handle(MessageEvent event, MusicManagerCache cache,
                       ScheduledExecutorService ses, MessageTokenizer mt)
    {
        if (mt.hasNext()) {
            event.getChannel().toggleTypingStatus();
            String url = mt.getRemainingContent().trim();
            GuildMusicManager gmm = cache.getGuildMusicManager(event.getGuild());
            cache.getAudioPlayerManager().loadItemOrdered(gmm, url,
                    new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack at)
                {
                    String song = String.format("[%s][%s] [%s](%s)",
                            LocalTime.MIN.plus(
                                    at.getPosition(),
                                    ChronoUnit.MILLIS)
                            .withNano(0)
                            .format(
                                    DateTimeFormatter.ISO_TIME),
                            LocalTime.MIN.plus(at
                                    .getDuration(), ChronoUnit.MILLIS)
                            .withNano(0)
                            .format(
                                    DateTimeFormatter.ISO_TIME),
                            at.getInfo().title,
                            at.getInfo().uri);

                    EmbedBuilder builder = baseBuilder().withDesc(song);

                    IMessage message = event.getChannel()
                            .sendMessage(builder.build());

                    ses.schedule(() -> {
                        message.delete();
                        event.getMessage().delete();
                    }, 10, TimeUnit.SECONDS);

                    play(event, gmm, at);
                }

                @Override
                public void playlistLoaded(AudioPlaylist ap)
                {
                    AtomicInteger position = new AtomicInteger();

                    String queue = ap.getTracks().stream().limit(20)
                            .map(track -> String.format("%02d: [%s] %s",
                                    position.getAndIncrement(),
                                    LocalTime.MIN.plus(
                                            track.getDuration(),
                                            ChronoUnit.MILLIS)
                                    .withNano(0)
                                    .format(DateTimeFormatter.ISO_TIME),
                                    track.getInfo().title)
                            ).collect(Collectors.joining("\n"));

                    EmbedBuilder builder = baseBuilder().withDesc(queue);

                    builder.withTitle(
                            String.format("queued %d songs", ap.getTracks()
                                    .size())
                    );

                    if (position.get() < ap.getTracks().size()) {
                        queue += "\n...";
                    }

                    IMessage message = event.getChannel()
                            .sendMessage(builder.build());

                    ses.schedule(() -> {
                        message.delete();
                        event.getMessage().delete();
                    }, 10, TimeUnit.SECONDS);
                    ap.getTracks().forEach(track -> play(event, gmm, track));
                }

                @Override
                public void noMatches()
                {
                    EmbedBuilder builder = baseBuilder().withDesc(
                            "Nothing found by " + url);

                    IMessage message = event.getChannel()
                            .sendMessage(builder.build());

                    ses.schedule(() -> {
                        message.delete();
                        event.getMessage().delete();
                    }, 10, TimeUnit.SECONDS);
                }

                @Override
                public void loadFailed(FriendlyException fe)
                {
                    EmbedBuilder builder = baseBuilder().withDesc(
                            "Could not play: " + fe
                            .getMessage());

                    IMessage message = event.getChannel()
                            .sendMessage(builder.build());

                    ses.schedule(() -> {
                        message.delete();
                        event.getMessage().delete();
                    }, 10, TimeUnit.SECONDS);
                }
            });
        } else {
            event.getChannel().sendMessage(
                    "you're supposed to give me link, dummy");
        }
    }

    private void play(MessageEvent event, GuildMusicManager musicManager,
                      AudioTrack track)
    {
        if (event.getGuild().getConnectedVoiceChannel() == null) {
            event.getGuild().getVoiceChannels().stream()
                    .filter(channel -> channel.getConnectedUsers().contains(
                            event.getAuthor()))
                    .findFirst()
                    .orElse(event.getGuild().getVoiceChannels().get(0))
                    .join();
        }

        musicManager.scheduler.queue(track);
    }

    private EmbedBuilder baseBuilder()
    {
        return new EmbedBuilder()
                .withColor(Color.BLUE)
                .withFooterText(
                        "this message will be deleted in 10 seconds")
                .withTimestamp(LocalDateTime.now());
    }

}
