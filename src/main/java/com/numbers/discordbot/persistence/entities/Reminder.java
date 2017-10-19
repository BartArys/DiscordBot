package com.numbers.discordbot.persistence.entities;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public class Reminder {

    private String dateTimeSerialized;

    private String message;

    private boolean handled;

    private long userId;

    private ObjectId objectId;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public String getDateTimeSerialized() {
        return dateTimeSerialized;
    }

    public void setDateTimeSerialized(String dateTimeSerialized) {
        this.dateTimeSerialized = dateTimeSerialized;
    }

    public void putDateTime(LocalDateTime localDateTime){
        setDateTimeSerialized(localDateTime.toString());
    }

    public LocalDateTime localDateTime(){
        return LocalDateTime.parse(dateTimeSerialized);
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
