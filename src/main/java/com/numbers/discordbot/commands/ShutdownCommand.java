package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import com.numbers.discordbot.filter.MessageFilter;

@Command
public class ShutdownCommand {

    @Command
    @MessageFilter(mentionsBot = true, eventType = MentionEvent.class,
                   regex = ".*kys.*")
    public void handle(MentionEvent event)
    {
        if (event.getAuthor().equals(event.getClient().getApplicationOwner())) {
            event.getChannel().sendMessage(":joy:  :gun:");
            System.exit(0);
        } else {
            event.getChannel().sendMessage("Check your privilege :smirk:");
        }
    }

}
