package com.numbers.discordbot.dependency;

import com.google.inject.*;
import com.mongodb.client.*;
import com.numbers.discordbot.audio.*;
import com.numbers.discordbot.persistence.*;
import sx.blah.discord.api.IDiscordClient;

public class CommandModule extends AbstractModule{

    private final MongoDB mdb;
    private final MusicManagerCache mmc;
    private final IDiscordClient client;

    public CommandModule(MongoDB mdb, MusicManagerCache mmc, IDiscordClient client)
    {
        this.mdb = mdb;
        this.mmc = mmc;
        this.client = client;
    }
    
    @Override
    protected void configure()
    {
        bind(MongoDatabase.class).toInstance(mdb.getDatabase());
        bind(MusicManagerCache.class).toInstance(mmc);
        bind(IDiscordClient.class).toInstance(client);
    }

}
