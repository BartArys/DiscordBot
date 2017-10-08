package com.numbers.discordbot.audio;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.track.playback.*;
import java.util.*;
import sx.blah.discord.handle.audio.*;

public class AudioProvider implements IAudioProvider {

    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    /**
     * @param audioPlayer Audio player to wrap.
     */
    public AudioProvider(AudioPlayer audioPlayer)
    {
        this.audioPlayer = audioPlayer;
    }

    @Override
    public boolean isReady()
    {
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide();
        }

        return lastFrame != null;
    }

    @Override
    public byte[] provide()
    {

        byte[] data = getFrame();

//        if(data != null){
//            while (data.length < 32) { //Chiper size
//        
//                byte[] add = getFrame();
//                if(add == null || add.length == 0){
//                    break;
//                }
//                
//                data = concat(data, add);
//            }
//        }
        return data;
    }

    private byte[] getFrame()
    {
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide();
        }

        byte[] data = lastFrame != null
                      ? lastFrame.data
                      : null;
        lastFrame = null;

        return data;
    }

    public byte[] concat(byte[] a, byte[] b)
    {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    @Override
    public int getChannels()
    {
        return 2;
    }

    @Override
    public AudioEncodingType getAudioEncodingType()
    {
        return AudioEncodingType.OPUS;
    }
}
