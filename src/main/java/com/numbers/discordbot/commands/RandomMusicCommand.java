package com.numbers.discordbot.commands;

import com.numbers.discordbot.network.reddit.RedditMusic;
import com.numbers.discordbot.*;
import com.numbers.discordbot.audio.*;
import com.numbers.discordbot.filter.*;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.tools.*;
import com.sedmelluq.discord.lavaplayer.track.*;
import java.util.*;
import java.util.stream.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;

@Command
public class RandomMusicCommand {

    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true,
                   regex = ".*music.*")
    public void handleRegex(MentionEvent event, MusicManagerCache cache,
                            RedditMusic rm)
    {
        handle(event, cache, rm);
    }

    @Command
    @MessageFilter(eventType = MessageEvent.class, prefixCheck = true,
                   regex = "music")
    public void handlePrefix(MessageEvent event, MusicManagerCache cache,
                             RedditMusic rm)
    {
        handle(event, cache, rm);
    }

    public void handle(MessageEvent event, MusicManagerCache cache,
                       RedditMusic rm)
    {
        GuildMusicManager gmm = cache.getGuildMusicManager(event.getGuild());
        List<String> urls = rm.getUrls();
        List<String> songs = new Random().ints(0, urls.size())
                .distinct()
                .limit(10)
                .mapToObj(urls::get).collect(Collectors.toList());

        for (String song : songs) {
            cache.getAudioPlayerManager().loadItemOrdered(gmm, song,
                    new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack at)
                {
                    gmm.scheduler.queue(at);
                }

                @Override
                public void playlistLoaded(AudioPlaylist ap)
                {
                }

                @Override
                public void noMatches()
                {
                }

                @Override
                public void loadFailed(FriendlyException fe)
                {
                }
            });
        }
    }

}
