package com.numbers.discordbot.commands;

import com.numbers.discordbot.filter.Filter;
import sx.blah.discord.api.events.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;

public class ShutdownCommand implements IListener<MentionEvent>{

    @Override
    @Filter(mentionsBot = true, eventType = MentionEvent.class, regex = ".*kys.*")
    public void handle(MentionEvent event)
    {
        event.getChannel().sendMessage(":joy:  :gun:");
        System.exit(0);
    }

}
