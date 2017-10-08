package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import com.numbers.discordbot.filter.*;
import com.numbers.discordbot.network.reddit.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.util.*;

@Command
public class ProgrammerHumorCommand {

    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true, regex = ".*programmerHumor")
    public void handleMention(MentionEvent event, ProgrammerHumor ph){
        List<ProgrammerHumor.ProgrammerPost> urls = ph.getPosts();
        ProgrammerHumor.ProgrammerPost post = urls.get(new Random().nextInt(urls.size()));
        EmbedBuilder builder = new EmbedBuilder().withColor(Color.BLUE).withImage(post.getUrl()).withTitle(post.getTitle());
        
        
        event.getChannel().sendMessage(builder.build());
    }
    
}
