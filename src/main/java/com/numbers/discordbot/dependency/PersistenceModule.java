package com.numbers.discordbot.dependency;

import com.google.inject.*;
import com.numbers.discordbot.persistence.*;

public class PersistenceModule extends AbstractModule{

    private final MongoDB mongoDB;

    public PersistenceModule(MongoDB mongoDB)
    {
        this.mongoDB = mongoDB;
    }
    
    @Override
    protected void configure()
    {
        bind(WeatherRepository.class).toInstance(new WeatherRepository(mongoDB.getDatabase()));
    }

}
