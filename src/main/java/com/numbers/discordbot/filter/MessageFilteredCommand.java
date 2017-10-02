package com.numbers.discordbot.filter;

import com.numbers.discordbot.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import sx.blah.discord.api.events.*;

public class MessageFilteredCommand {

    private final boolean mentionsBot;
    private final Predicate<String> regex;
    private final String prefix;
    private final Class<? extends Event> eventType;
    private final Method command;
    private final Object source;

    public MessageFilteredCommand(Object cmd)
    {
        Method command = Arrays.stream(cmd.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(Command.class) && method
                        .isAnnotationPresent(
                                MessageFilter.class)).findAny().orElse(null);

        if (command == null)
            throw new IllegalArgumentException(
                    "IListener requires annotated method handle");

        MessageFilter filter = command.getAnnotation(MessageFilter.class);

        if (filter == null)
            throw new IllegalArgumentException("Filter should be present");

        this.mentionsBot = filter.mentionsBot();
        this.regex = (String string) -> string.matches(filter.regex());
        this.prefix = filter.startsWith();
        this.eventType = filter.eventType();
        this.command = command;
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
    
}
