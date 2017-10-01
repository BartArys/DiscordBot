package com.numbers.discordbot.audio;

import com.sedmelluq.discord.lavaplayer.player.*;
import sx.blah.discord.handle.obj.*;

public interface MusicManagerCache {

    GuildMusicManager getGuildMusicManager(IGuild fromGuild);
    
    AudioPlayerManager getAudioPlayerManager();
}
