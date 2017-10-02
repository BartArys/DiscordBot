package com.numbers.discordbot.audio;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.player.event.*;
import com.sedmelluq.discord.lavaplayer.track.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public Stream<AudioTrack> getQueueStream(){
        return queue.stream();
    }
    
    public long getQueueSize(){
        return queue.size();
    }
    
    public boolean isQueueEmpty(){
        return queue.isEmpty();
    }
    
    public void clear(){
        queue.clear();
    }
    
    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void nextTrack() {
        player.startTrack(queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }
}