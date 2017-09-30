package com.numbers.discordbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.*;
import java.util.*;
import sx.blah.discord.handle.obj.*;

public class Audio {

    private static final Map<String,GuildMusicManager> MUSIC_MANS = new HashMap<>();
    private static AudioPlayerManager apm;
    
    public static void Init(){
        apm = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(apm);
    }
    
    public static GuildMusicManager getGuildAudioPlayer(IGuild guild){
        String guildId = guild.getStringID();
        GuildMusicManager musicManager = MUSIC_MANS.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(apm);
            MUSIC_MANS.put(guildId, musicManager);
        }
        
        guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

        return musicManager;
    }

    public static AudioPlayerManager getApm()
    {
        return apm;
    }
    
}
