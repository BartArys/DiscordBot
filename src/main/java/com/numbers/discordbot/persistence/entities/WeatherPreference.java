package com.numbers.discordbot.persistence.entities;

import org.bson.types.*;

public class WeatherPreference {

    public static WeatherPreference parseFrom(String content){
        content = content.trim();
        String[] split = content.split(",");
        if(split.length != 2) return null;
        return new WeatherPreference(split[0].trim(), split[1].trim());
    }

    private ObjectId id;
    private String userId;
    private String city;
    private String country;

    public WeatherPreference()
    {
    }

    public WeatherPreference(String city, String country)
    {
        this.city = city;
        this.country = country;
    }

    
    
    
    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public ObjectId  getId()
    {
        return id;
    }

    public void setId(ObjectId id)
    {
        this.id = id;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

}
