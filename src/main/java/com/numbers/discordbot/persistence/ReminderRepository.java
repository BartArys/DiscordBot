package com.numbers.discordbot.persistence;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.numbers.discordbot.persistence.entities.Reminder;

import java.util.ArrayList;
import java.util.List;

public class ReminderRepository {

    private final MongoCollection<Reminder> reminders;

    @Inject
    public ReminderRepository(MongoDB mongoDB){
        this.reminders = mongoDB.getDatabase().getCollection("reminders", Reminder.class);
    }

    public void addReminder(Reminder reminder){
        reminders.insertOne(reminder);
    }

    public void updateReminder(Reminder reminder){
        reminders.replaceOne(Filters.eq("id", reminder.getObjectId()), reminder);
    }

    public List<Reminder> getReminders(){
        List<Reminder> rems = new ArrayList<>();
        for(Reminder r : reminders.find()){
            rems.add(r);
        }
        return rems;
    }

    public void deletReminders(List<Reminder> r){
            reminders.deleteMany(Filters.all("id", r));
    }

    public void deleteReminder(Reminder reminder){
        reminders.deleteOne(Filters.eq("id", reminder.getObjectId()));
    }


}
