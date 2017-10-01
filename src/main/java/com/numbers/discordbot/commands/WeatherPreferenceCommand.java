package com.numbers.discordbot.commands;

import com.numbers.discordbot.*;
import com.numbers.discordbot.filter.*;
import com.numbers.discordbot.persistence.*;
import com.numbers.discordbot.persistence.entities.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.util.*;

@Command
public class WeatherPreferenceCommand {

    @Command
    @Filter(eventType = MentionEvent.class, mentionsBot = true, regex = ".*weatherPreference\\s.+")
    public void handle(MentionEvent event, WeatherRepository repository){
        MessageTokenizer tokenizer = new MessageTokenizer(event.getMessage());
        tokenizer.nextMention();
        tokenizer.nextWord();
        
        if(tokenizer.hasNext()){
            WeatherPreference preference = WeatherPreference.parseFrom(tokenizer.getRemainingContent());
            preference.setUserId(event.getAuthor().getStringID());
            if(repository.getPreferenceFromUser(event.getAuthor()).isPresent()){
                repository.update(preference);
            }else{
                repository.put(preference);
            }
        }
    
    }
    
}
