package com.numbers.discordbot.filter;

import java.util.*;
import sx.blah.discord.api.events.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;

public class EventListener implements IListener<MessageEvent>{

    private final Map<Class,Set<FilteredListener>> map;

    public EventListener()
    {
        this.map = new HashMap<>();
    }
    
    public void addListener(IListener iListener){
        FilteredListener fl = new FilteredListener(iListener);
        Set<FilteredListener> listeners = map.get(fl.getEventType());
        if(listeners == null){
            Set<FilteredListener> list = new HashSet<>();
            list.add(fl);
            map.put(fl.getEventType(), list);
        }else{
            listeners.add(fl);
        }
    }

    @Override
    public void handle(MessageEvent event)
    {
        map.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(event.getClass()))
                .map(Map.Entry::getValue)
                .flatMap(Set::stream)
                .filter(fl -> fl.matchesRegex(event.getMessage().getContent()))
                .forEach(fl -> fl.invoke(event));
    }
    
    
}
