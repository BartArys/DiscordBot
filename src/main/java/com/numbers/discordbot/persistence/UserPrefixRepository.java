package com.numbers.discordbot.persistence;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.numbers.discordbot.persistence.entities.*;
import java.util.*;
import sx.blah.discord.handle.obj.*;

public class UserPrefixRepository {

    
    private final MongoCollection<UserPrefix> prefixes;

    public UserPrefixRepository(MongoDatabase database)
    {
        prefixes = database
                .getCollection("prefix", UserPrefix.class);
    }

    public Optional<UserPrefix> getPreferenceFromUser(IUser user)
    {
        UserPrefix prefix = prefixes
                .find(Filters.eq("userId", user.getStringID()))
                .first();
        return Optional.ofNullable(prefix);
    }

    public void put(UserPrefix prefix)
    {
        prefixes.insertOne(prefix);
    }

    public void update(UserPrefix prefix)
    {
        prefixes.replaceOne(Filters.eq("userId", prefix.getUserId()),
                prefix);
    }
    
}
