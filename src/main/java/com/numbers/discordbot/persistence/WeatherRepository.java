package com.numbers.discordbot.persistence;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.numbers.discordbot.persistence.entities.*;
import java.util.*;
import sx.blah.discord.handle.obj.*;

public class WeatherRepository {

    private final MongoCollection<WeatherPreference> weatherPreferences;

    public WeatherRepository(MongoDatabase database)
    {
        weatherPreferences = database
                .getCollection("weather", WeatherPreference.class);
    }

    public Optional<WeatherPreference> getPreferenceFromUser(IUser user)
    {
        WeatherPreference preference = weatherPreferences
                .find(Filters.eq("userId", user.getStringID()))
                .first();
        return Optional.ofNullable(preference);
    }

    public void put(WeatherPreference preference)
    {
        weatherPreferences.insertOne(preference);
    }

    public void update(WeatherPreference preference)
    {
        weatherPreferences.replaceOne(Filters.eq("userId", preference.getId()),
                preference);
    }

}
