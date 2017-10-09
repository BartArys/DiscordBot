package com.numbers.discordbot.commands;

import com.numbers.discordbot.Command;
import com.numbers.discordbot.audio.GuildMusicManager;
import com.numbers.discordbot.audio.MusicManagerCache;
import com.numbers.discordbot.audio.SilentAudioResultHandler;
import com.numbers.discordbot.filter.MessageFilter;
import com.numbers.discordbot.network.reddit.RedditMusic;
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
            cache.getAudioPlayerManager().loadItemOrdered(gmm, song, new SilentAudioResultHandler(gmm.getScheduler()));
        }
    }

}
