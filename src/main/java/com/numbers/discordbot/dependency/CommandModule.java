package com.numbers.discordbot.dependency;

import com.google.inject.*;
import com.mongodb.client.*;
import com.numbers.discordbot.audio.*;
import com.numbers.discordbot.persistence.*;

public class CommandModule extends AbstractModule{

    private final MongoDB mdb;
    private final MusicManagerCache mmc;
    
    public CommandModule(MongoDB mdb, MusicManagerCache mmc)
    {
        this.mdb = mdb;
        this.mmc = mmc;
    }
    
    @Override
    protected void configure()
    {
        bind(MongoDatabase.class).toInstance(mdb.getDatabase());
        bind(MusicManagerCache.class).toInstance(mmc);
    }

}
