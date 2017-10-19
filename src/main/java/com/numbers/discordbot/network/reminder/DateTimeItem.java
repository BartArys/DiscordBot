package com.numbers.discordbot.network.reminder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DateTimeItem {

    private String value;
    private String grain;

    public String getValue() {
        return value;
    }

    public String getGrain() {
        return grain;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setGrain(String grain) {
        this.grain = grain;
    }
}
