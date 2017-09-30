package com.numbers.discordbot.commands;

import com.numbers.discordbot.audio.*;
import com.numbers.discordbot.filter.*;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.tools.*;
import com.sedmelluq.discord.lavaplayer.track.*;
import sx.blah.discord.api.events.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

public class PlayMusicCommand implements IListener<MentionEvent>{

    @Override
    @Filter(eventType =  MentionEvent.class, mentionsBot = true, regex = ".*play\\s.+")
    public void handle(MentionEvent event) 
    {
        MessageTokenizer mt = new MessageTokenizer(event.getMessage());
        mt.nextMention(); //mention
        mt.nextWord(); //play
        
        if(mt.hasNext()){
            String url = mt.getRemainingContent().trim();
            GuildMusicManager gmm = Audio.getGuildAudioPlayer(event.getGuild());
            Audio.getApm().loadItemOrdered(gmm, url, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack at)
                {
                    event.getChannel().sendMessage(String.format("+ %s[1/%d]", at.getInfo().title, gmm.scheduler.getQueueStream().count()));
                    play(event,gmm, at);
                }

                @Override
                public void playlistLoaded(AudioPlaylist ap)
                {
                    String message = String.format("+%s[%d/%d]", ap.getName(), ap.getTracks().size(), gmm.scheduler.getQueueStream().count());
                    event.getChannel().sendMessage(message);
                    ap.getTracks().forEach(track -> play(event, gmm, track));
                }

                @Override
                public void noMatches()
                {
                    event.getChannel().sendMessage("Nothing found by " + url);
                }

                @Override
                public void loadFailed(FriendlyException fe)
                {
                    event.getChannel().sendMessage("Could not play: " + fe.getMessage());
                }
            });
        }else{
            event.getChannel().sendMessage("you're supposed to give me link, dummy");
        }
    }

    private void play(MessageEvent event , GuildMusicManager musicManager, AudioTrack track){
        if(event.getGuild().getConnectedVoiceChannel() == null){
            event.getGuild().getVoiceChannels().stream().findFirst().ifPresent(IVoiceChannel::join);
        }

        musicManager.scheduler.queue(track);
    }
    
}
