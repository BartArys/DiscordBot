package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import java.util.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;
import com.numbers.discordbot.filter.MessageFilter;

@Command
public class JoinVoiceCommand {

    @Command
    @MessageFilter(mentionsBot = true, 
            regex = ".*join.*",
            eventType = MentionEvent.class)
    public void handle(MentionEvent event)  
    {
        MessageTokenizer tokenizer = event.getMessage().tokenize();
        tokenizer.nextMention(); //bot
        tokenizer.nextWord(); //join
        
        handleCommand(event, tokenizer);
    }
    
    @Command
    @MessageFilter(eventType = 
            MessageEvent.class, 
            prefixCheck = true, 
            startsWith = "join")
    public void handle(MessageEvent event){
        MessageTokenizer tokenizer = event.getMessage().tokenize();
        tokenizer.nextWord(); //command
        tokenizer.nextWord(); //join
        
        handleCommand(event, tokenizer);
    }
    
    private void handleCommand(MessageEvent event, MessageTokenizer tokenizer){
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

    private void joinUser(MessageEvent event, IUser user)
    {
        IVoiceChannel channel = event.getGuild().getVoiceChannels().stream()
                .filter(chnl -> chnl.getConnectedUsers().contains(user))
                .findAny().orElse(event.getGuild().getVoiceChannels().get(0));

        joinChannel(event, channel);
    }

    private void joinChannel(MessageEvent event, IVoiceChannel channel)
    {
        if (event.getGuild().getConnectedVoiceChannel() != channel) {
            if(event.getGuild().getConnectedVoiceChannel() != null)
                event.getGuild().getConnectedVoiceChannel().leave();
            channel.join();
        }
    }

}
