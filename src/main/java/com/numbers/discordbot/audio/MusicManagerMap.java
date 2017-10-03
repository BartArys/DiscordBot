package com.numbers.discordbot.audio;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.*;
import com.sedmelluq.discord.lavaplayer.source.youtube.*;
import java.util.*;
import sx.blah.discord.handle.obj.*;

public class MusicManagerMap implements MusicManagerCache {

    private final Map<String,GuildMusicManager> cache;
    private final AudioPlayerManager apm;
    
    public MusicManagerMap()
    {
        this.cache = new HashMap<>();
        apm = new DefaultAudioPlayerManager();
        apm.registerSourceManager(new YoutubeAudioSourceManager(true));
        AudioSourceManagers.registerRemoteSources(apm);
    }
    
    @Override
    public GuildMusicManager getGuildMusicManager(IGuild fromGuild)
    {
        GuildMusicManager gmm = cache.get(fromGuild.getStringID());
        if(gmm == null){
            gmm = new GuildMusicManager(apm);
            cache.put(fromGuild.getStringID(), gmm);
            fromGuild.getAudioManager().setAudioProvider(gmm.getAudioProvider());
        }
        return gmm;
    }

    public Map<String, GuildMusicManager> getCache()
    {
        return cache;
    }

    @Override
    public AudioPlayerManager getAudioPlayerManager()
    {
        return apm;
    }

}
