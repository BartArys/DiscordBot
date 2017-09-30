package com.numbers.discordbot.filter;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import sx.blah.discord.api.events.*;

public class FilteredListener {

    private final boolean mentionsBot;
    private final Predicate<String> regex;
    private final String prefix;
    private final Class<? extends Event> eventType;
    private final IListener iListener;

    public FilteredListener(IListener iListener)
    {
        Method[] methods = iListener.getClass().getMethods();
        
        Method handleMethod = Arrays.stream(iListener.getClass().getMethods())
                .filter(method -> "handle".equals(method.getName()) && method
                        .getParameterCount() == 1 && method.isAnnotationPresent(
                                Filter.class)).findAny().orElse(null);

        if (handleMethod == null)
            throw new IllegalArgumentException(
                    "IListener requires annotated method handle");

        Filter filter = handleMethod.getAnnotation(Filter.class);

        if (filter == null)
            throw new IllegalArgumentException("Filter should be present");

        String regex = filter.regex();
        
        this.mentionsBot = filter.mentionsBot();
        this.regex = (String string) -> string.matches(regex);
        this.prefix = filter.startsWith();
        this.eventType = filter.eventType();
        this.iListener = iListener;
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

    public void invoke(Event e)
    {
        iListener.handle(e);
    }

}
