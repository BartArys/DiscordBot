package com.numbers.discordbot.filter;

import com.google.inject.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;
import sx.blah.discord.api.events.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;

public class MesageEventListener implements IListener<MessageEvent>{

    private final Map<Class,Set<MessageFilteredCommand>> map;
    private final Injector injector;
    
    public MesageEventListener(Injector injector)
    {
        this.map = new HashMap<>();
        this.injector = injector;
    }
    
    public void addCommand(Object command){
        MessageFilteredCommand fc = new MessageFilteredCommand(command);
        Set<MessageFilteredCommand> listeners = map.get(fc.getEventType());
        if(listeners == null){
            listeners = new HashSet<>();
            map.put(fc.getEventType(), listeners);
        }
        listeners.add(fc);
    }

    @Override
    public void handle(MessageEvent event)
    {
        map.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(event.getClass()))
                .map(Map.Entry::getValue)
                .flatMap(Set::stream)
                .filter(fc -> fc.matchesRegex(event.getMessage().getContent()))
                .forEach(fc -> invokeCommand(fc, event));
    }
    
    private void invokeCommand(MessageFilteredCommand fc, MessageEvent event){
        try {
            Class eventType = fc.getEventType();
            Class[] paramTypes = fc.getCommand().getParameterTypes();
            Object[] parameters = new Object[paramTypes.length];
            for(int i = 0; i < paramTypes.length; i++){
                Class type = paramTypes[i];
                if(type.isAssignableFrom(eventType)){
                    parameters[i] = event;
                }else{
                    parameters[i] = injector.getInstance(type);
                }
            }
            fc.getCommand().invoke(fc.getSource(), parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(MesageEventListener.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    
}
