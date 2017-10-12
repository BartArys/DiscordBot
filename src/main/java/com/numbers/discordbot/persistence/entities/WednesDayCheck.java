package com.numbers.discordbot.persistence.entities;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public class WednesDayCheck {
    private ObjectId objectId;

    private LocalDateTime lastCheck;

    public LocalDateTime getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(LocalDateTime lastCheck) {
        this.lastCheck = lastCheck;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }
}
