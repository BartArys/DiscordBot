package com.numbers.discordbot.persistence;

import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.codecs.configuration.*;
import org.bson.codecs.pojo.*;

public class MongoDB {

    private final MongoDatabase database;
    
    public MongoDB()
    {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder().automatic(true).build()
                ));

        MongoClient client = new MongoClient("localhost",
                MongoClientOptions
                .builder()
                .codecRegistry(pojoCodecRegistry)
                .build()
        );
        
        database = client.getDatabase("DiscordBot");
    }

    public MongoDatabase getDatabase()
    {
        return database;
    }
    
}
