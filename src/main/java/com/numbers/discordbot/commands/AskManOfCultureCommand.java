package com.numbers.discordbot.commands;

import com.numbers.discordbot.Command;
import com.numbers.discordbot.filter.MessageFilter;
import com.numbers.discordbot.network.eightball.Response;
import com.numbers.discordbot.persistence.entities.UserPrefix;
import com.numbers.jttp.Jttp;
import com.numbers.jttp.response.JsonHttpResponse;
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageTokenizer;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Command(name = "8ball Of Culture")
public class AskManOfCultureCommand {
    
    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true,
                   regex = ".+\\?", readableUsage = "{$words}?")
    public void handle(MentionEvent event, Jttp jttp) throws UnsupportedEncodingException
    {
        MessageTokenizer tokenizer = event.getMessage().tokenize();
        tokenizer.nextMention();
        sendMessage(event, jttp, tokenizer.getRemainingContent());
    }
    
    @Command
    @MessageFilter(eventType = MessageEvent.class, prefixCheck = true,
                   regex = ".+\\?", readableUsage = "{$words}?")
    public void handlePrefix(MessageEvent event, Jttp jttp, UserPrefix prefix) throws UnsupportedEncodingException
    {
        sendMessage(event, jttp, event.getMessage().getContent().replace(prefix.getPrefix(),""));
    }


    private void sendMessage(MessageEvent event, Jttp jttp, String question) throws UnsupportedEncodingException {
        event.getChannel().toggleTypingStatus();
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(Color.PINK);
        builder.withTitle("man of culture says:");
        
        JsonHttpResponse<Response> response
                = jttp.get("https://8ball.delegator.com/magic/JSON/" + URLEncoder.encode(question, "UTF-8"))
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
