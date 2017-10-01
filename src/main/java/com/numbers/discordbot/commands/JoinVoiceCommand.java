package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import com.numbers.discordbot.filter.Filter;
import java.util.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

@Command
public class JoinVoiceCommand {

    @Command
    @Filter(mentionsBot = true, 
            regex = ".*join.*",
            eventType = MentionEvent.class)
    public void handle(MentionEvent event)  
    {
        MessageTokenizer tokenizer = new MessageTokenizer(event.getMessage());
        tokenizer.nextMention(); //bot
        tokenizer.nextWord(); //join
        
        if(tokenizer.hasNext()){
            if(tokenizer.hasNextMention()){
                MessageTokenizer.MentionToken token = tokenizer.nextMention();
                String content = token.getContent();
                content = content.substring(3, content.length() - 2);
                IUser mention = event.getGuild().getUserByID(Long.parseLong(content));
                if(mention != null){
                    joinUser(event, mention);
                }else{
                    joinUser(event, event.getAuthor());
                }
            }else{
                String channelName = tokenizer.nextWord().getContent();
                System.out.println(channelName);
                List<IVoiceChannel> channels = event.getGuild().getVoiceChannelsByName(channelName);
                if(!channels.isEmpty()){
                    joinChannel(event, channels.get(0));
                }else{
                    joinUser(event, event.getAuthor());
                }
            }
        }else{
            joinUser(event, event.getAuthor());
        }
    }

    private void joinUser(MentionEvent event, IUser user)
    {
        IVoiceChannel channel = event.getGuild().getVoiceChannels().stream()
                .filter(chnl -> chnl.getConnectedUsers().contains(user))
                .findAny().orElse(event.getGuild().getVoiceChannels().get(0));

        joinChannel(event, channel);
    }

    private void joinChannel(MentionEvent event, IVoiceChannel channel)
    {
        if (event.getGuild().getConnectedVoiceChannel() != channel) {
            if(event.getGuild().getConnectedVoiceChannel() != null)
                event.getGuild().getConnectedVoiceChannel().leave();
            channel.join();
        }
    }

}
