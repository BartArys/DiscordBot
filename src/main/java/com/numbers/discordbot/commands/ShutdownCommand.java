package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import com.numbers.discordbot.filter.MessageFilter;
import sx.blah.discord.handle.obj.*;

@Command(name =  "Shut Down")
public class ShutdownCommand {

    @Command
    @MessageFilter(mentionsBot = true, eventType = MentionEvent.class,
                   regex = ".*kys", readableUsage = "kys")
    public void handle(MentionEvent event)
    {
        handleCommand(event.getAuthor(), event.getChannel());
    }
    
    @Command
    @MessageFilter(eventType = MessageEvent.class, prefixCheck = true, regex = "kys", readableUsage = "kys")
    public void handlePrefix(MessageEvent event){
        handleCommand(event.getAuthor(), event.getChannel());
    }
   
    
    private void handleCommand(IUser user, IChannel channel){
        if (user.equals(user.getClient().getApplicationOwner())) {
            channel.sendMessage(":joy:  :gun:");
            System.exit(0);
        } else {
            channel.sendMessage("Check your privilege :smirk:");
        }
    }

}
