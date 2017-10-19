package com.numbers.discordbot.persistence.entities;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public class WednesDayCheck {
    private ObjectId objectId;

    private String lastCheckString;

    public LocalDateTime lastCheckDateTime() {
        return LocalDateTime.parse(lastCheckString);
    }

    public void LastCheckLocalDateTime(LocalDateTime lastCheck) {
        this.lastCheckString = lastCheck.toString();
    }

    public String getLastCheckString() {
        return lastCheckString;
    }

    public void setLastCheckString(String lastCheckString) {
        this.lastCheckString = lastCheckString;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }
}
