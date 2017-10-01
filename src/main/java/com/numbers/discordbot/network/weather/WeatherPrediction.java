package com.numbers.discordbot.network.weather;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherPrediction {

    private final String main;
    private final String description;
    private final String icon;

    @JsonCreator
    public WeatherPrediction(@JsonProperty("main") String main,
                             @JsonProperty("description") String description,
                             @JsonProperty("icon") String icon)
    {
        this.main = main;
        this.description = description;
        this.icon = icon;
    }

    public String getDescription()
    {
        return description;
    }

    public String getIcon()
    {
        return icon;
    }

    public String getMain()
    {
        return main;
    }

}
