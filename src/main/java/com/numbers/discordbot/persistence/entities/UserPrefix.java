package com.numbers.discordbot.persistence.entities;

import org.bson.types.*;

public class UserPrefix {

    private ObjectId id;
    private String userId;
    private String prefix;

    public static final UserPrefix DEFAULT = new UserPrefix("", ";b");
    
    public UserPrefix(String userId, String prefix)
    {
        this.userId = userId;
        this.prefix = prefix;
    }
    
    public String getPrefix()
    {
        return prefix;
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

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }
    
    public ObjectId getId()
    {
        return id;
    }

}
