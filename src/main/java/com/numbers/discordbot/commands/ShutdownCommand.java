package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import com.numbers.discordbot.filter.Filter;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;

@Command
public class ShutdownCommand{

    @Command
    @Filter(mentionsBot = true, eventType = MentionEvent.class, regex = ".*kys.*")
    public void handle(MentionEvent event)
    {
        event.getChannel().sendMessage(":joy:  :gun:");
        System.exit(0);
    }

}
