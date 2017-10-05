package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import com.numbers.discordbot.filter.*;
import com.numbers.discordbot.network.eightball.*;
import com.numbers.jttp.*;
import com.numbers.jttp.response.*;
import java.awt.*;
import java.time.*;
import java.util.concurrent.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

@Command
public class AskManOfCultureCommand {
    
    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true,
                   regex = ".+\\?")
    public void handle(MentionEvent event, ScheduledExecutorService ses,
                       Jttp jttp)
    {
        
        event.getChannel().toggleTypingStatus();
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(Color.PINK);
        builder.withTitle("man of culture says:");
        
        MessageTokenizer tokenizer = new MessageTokenizer(event.getMessage());
        tokenizer.nextMention();
        
        JsonHttpResponse<Response> response = 
                jttp.get("https://8ball.delegator.com/magic/JSON/" + tokenizer
                .getRemainingContent().replaceAll("\\s", "%20"))
                .asObject(Response.class)
                .join();
        
        if(response.getResponse().getResponse().getType().equals("Contrary")){
            builder.withImage("https://i.imgur.com/x9SaOhx.png");
        }else{
            builder.withImage("https://i.imgur.com/bDEZAT9.png");
        }
        
        builder.appendDesc(response.getResponse().getResponse().getAnswer());
        
        IMessage message = event.getChannel().sendMessage(builder.build());
    }
}
