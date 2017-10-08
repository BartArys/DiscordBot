package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import com.numbers.discordbot.filter.*;
import com.numbers.discordbot.network.eightball.*;
import com.numbers.discordbot.persistence.entities.*;
import com.numbers.jttp.*;
import com.numbers.jttp.response.*;
import java.awt.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.util.*;

@Command
public class AskManOfCultureCommand {
    
    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true,
                   regex = ".+\\?")
    public void handle(MentionEvent event, Jttp jttp)
    {
        MessageTokenizer tokenizer = event.getMessage().tokenize();
        tokenizer.nextMention();
        sendMessage(event, jttp, tokenizer.getRemainingContent());
    }
    
    @Command
    @MessageFilter(eventType = MessageEvent.class, prefixCheck = true,
                   regex = ".+\\?")
    public void handlePrefix(MessageEvent event, Jttp jttp, UserPrefix prefix)
    {
        sendMessage(event, jttp, event.getMessage().getContent().replace(prefix.getPrefix(),""));
    }
    
    private void sendMessage(MessageEvent event, Jttp jttp, String question)
    {
        event.getChannel().toggleTypingStatus();
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(Color.PINK);
        builder.withTitle("man of culture says:");
        
        JsonHttpResponse<Response> response
                = jttp.get("https://8ball.delegator.com/magic/JSON/" + question.replaceAll("\\s", "%20"))
                .asObject(Response.class)
                .join();
        
        if (response.getResponse().getResponse().getType().equals("Contrary")) {
            builder.withImage("https://i.imgur.com/x9SaOhx.png");
        } else {
            builder.withImage("https://i.imgur.com/bDEZAT9.png");
        }
        
        builder.appendDesc(response.getResponse().getResponse().getAnswer());
        event.getChannel().sendMessage(builder.build());
    }
}
