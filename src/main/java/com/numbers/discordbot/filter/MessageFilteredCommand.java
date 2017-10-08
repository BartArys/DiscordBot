package com.numbers.discordbot.filter;

import com.numbers.discordbot.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import sx.blah.discord.api.events.*;

public class MessageFilteredCommand {

    private final boolean mentionsBot;
    private final Predicate<String> regex;
    private final String prefix;
    private final Class<? extends Event> eventType;
    private final Method command;
    private final Object source;
    private final boolean prefixCommand;

    public static List<MessageFilteredCommand> find(Object cmd)
    {
        return Arrays.stream(cmd.getClass().getMethods())
                .filter(method
                        -> method.isAnnotationPresent(Command.class)
                        && method.isAnnotationPresent(MessageFilter.class))
                .map(method -> new MessageFilteredCommand(cmd, method))
                .collect(Collectors.toList());
    }

    public MessageFilteredCommand(Object cmd, Method command)
    {
        MessageFilter filter = command.getAnnotation(MessageFilter.class);

        if (filter == null)
            throw new IllegalArgumentException("Filter should be present");

        this.mentionsBot = filter.mentionsBot();
        if(filter.regex().isEmpty()){
            this.regex = string -> true;
        }else{
            this.regex = (String string) -> string.matches(filter.regex());
        }
        this.prefix = filter.startsWith();
        this.eventType = filter.eventType();
        this.command = command;
        this.prefixCommand = filter.prefixCheck();
        source = cmd;
    }

    public boolean isMentionsBot()
    {
        return mentionsBot;
    }

    public boolean matchesRegex(String message)
    {
        return regex.test(message);
    }
    
    public boolean matchesRegex(String message, String prefix){
        return regex.test(message.replace(prefix, ""));
    }

    public String getPrefix()
    {
        return prefix;
    }

    public Class<? extends Event> getEventType()
    {
        return eventType;
    }

    public Method getCommand()
    {
        return command;
    }

    public Object getSource()
    {
        return source;
    }

    public boolean isPrefixCommand()
    {
        return prefixCommand;
    }

}
