package com.numbers.discordbot.filter;

import com.numbers.discordbot.*;
import java.lang.reflect.*;
import java.util.*;

public class VoiceFilteredCommand {

    private final Object source;
    private final Method command;
    private final Class eventType;

    public VoiceFilteredCommand(Object source)
    {
        Method cmd = Arrays.stream(source.getClass().getMethods())
                .filter(method -> 
                        method.isAnnotationPresent(VoiceCommand.class) && 
                        method.isAnnotationPresent(VoiceFilter.class))
                .findAny()
                .orElse(null);

        if (cmd == null)
            throw new IllegalArgumentException(
                    "method requires VoiceCommand and VoiceFilter");

        VoiceFilter filter = cmd.getAnnotation(VoiceFilter.class);

        if (filter == null)
            throw new IllegalArgumentException("Filter should be present");

        this.eventType = filter.eventType();
        this.command = cmd;
        this.source = source;
    }
    
    public Class getEventType(){
        return eventType;
    }
    
    public Method getCommand(){
        return command;
    }
    
    public Object getSource(){
        return source;
    }
    
}
