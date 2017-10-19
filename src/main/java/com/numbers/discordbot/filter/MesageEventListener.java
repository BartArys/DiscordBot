package com.numbers.discordbot.filter;

import com.google.inject.Injector;
import com.numbers.discordbot.persistence.UserPrefixRepository;
import com.numbers.discordbot.persistence.entities.UserPrefix;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEmbedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MesageEventListener implements IListener<MessageEvent> {

    private final Map<Class, Set<MessageFilteredCommand>> map;
    private final Injector injector;
    private final UserPrefixRepository upr;

    public MesageEventListener(Injector injector)
    {
        this.map = new HashMap<>();
        this.injector = injector;
        this.upr = injector.getInstance(UserPrefixRepository.class);
    }

    public void addCommand(Object command)
    {
        System.out.print(command.getClass().getSimpleName());
        List<MessageFilteredCommand> fcs = MessageFilteredCommand.find(command);
        for (MessageFilteredCommand fc : fcs) {
            System.out.print(" " + fc.getCommand().getName());
            Set<MessageFilteredCommand> listeners = map.get(fc.getEventType());
            if (listeners == null) {
                listeners = new HashSet<>();
                map.put(fc.getEventType(), listeners);
            }
            listeners.add(fc);
        }
        System.out.println();
    }

    @Override
    public void handle(MessageEvent event)
    {
        if (event instanceof MessageDeleteEvent || event instanceof MessageEmbedEvent){
            return;
        }
        
        System.out.println("message type: " + event.getClass().getName());
        System.out.println(">> " + event.getMessage().getContent());
        
        
        map.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(event
                        .getClass()))
                .map(Map.Entry::getValue)
                .flatMap(Set::stream)
                .filter(fc -> matches(event, fc))
//                .peek(fc -> System.out.println(fc.getCommand().getName()))
                .forEach(fc -> invokeCommand(fc, event));
    }
    
    private boolean matches(MessageEvent event, MessageFilteredCommand command){
        String prefix = command.getPrefix();
        String content = event.getMessage().getContent();
        if(command.isPrefixCommand()){
            UserPrefix up = upr.getPreferenceFromUser(event.getAuthor()).orElse(UserPrefix.DEFAULT);
            content = content.replace(up.getPrefix(), "").trim();
            if(!content.startsWith(prefix)) return false;
            if(!command.matchesRegex(content, prefix)) return false;
        }else{
            if(!command.matchesRegex(content)) return false;
            if(!content.startsWith(prefix)) return false;
        }
        return !(command.isMentionsBot() && !event.getMessage().getMentions().contains(event.getClient().getOurUser()));
    }

    private void invokeCommand(MessageFilteredCommand fc, MessageEvent event)
    {
        try {
            Class eventType = fc.getEventType();
            Class[] paramTypes = fc.getCommand().getParameterTypes();
            Object[] parameters = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                Class type = paramTypes[i];
                if (type.isAssignableFrom(eventType)) {
                    parameters[i] = event;
                } else if(type.isAssignableFrom(UserPrefix.class)){
                    UserPrefix up = upr.getPreferenceFromUser(event.getAuthor()).orElse(UserPrefix.DEFAULT);
                    parameters[i] = up;
                }else {
                    parameters[i] = injector.getInstance(type);
                }
            }
            fc.getCommand().invoke(fc.getSource(), parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(MesageEventListener.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (Exception ex){
            event.getChannel().sendMessage("Tell whatever idiot that programmed me he fucked up");
            Logger.getLogger(MesageEventListener.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

}
