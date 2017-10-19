package com.numbers.discordbot.network.reminder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Entities {

    private List<ContactItem> contact;
    private List<DateTimeItem> datetime;
    private List<ReminderItem> reminder;

    public List<ContactItem> getContact() {
        return contact;
    }

    public List<DateTimeItem> getDatetime() {
        return datetime;
    }

    public List<ReminderItem> getReminder() {
        return reminder;
    }

    public void setContact(List<ContactItem> contact) {
        this.contact = contact;
    }

    public void setDatetime(List<DateTimeItem> datetime) {
        this.datetime = datetime;
    }

    public void setReminder(List<ReminderItem> reminder) {
        this.reminder = reminder;
    }
}
