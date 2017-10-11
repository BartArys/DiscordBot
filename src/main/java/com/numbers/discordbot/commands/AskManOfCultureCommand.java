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

@Command(name = "8ball Of Culture")
public class AskManOfCultureCommand {
    
    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true,
                   regex = ".+\\?", readableUsage = "{$words}?")
    public void handle(MentionEvent event, Jttp jttp)
    {
        MessageTokenizer tokenizer = event.getMessage().tokenize();
        tokenizer.nextMention();
        sendMessage(event, jttp, tokenizer.getRemainingContent());
    }
    
    @Command
    @MessageFilter(eventType = MessageEvent.class, prefixCheck = true,
                   regex = ".+\\?", readableUsage = "{$words}?")
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
