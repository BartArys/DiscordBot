package com.numbers.discordbot.network.weather;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MainPrediction {

    private final double temperature;
    private final double minTemperature;
    private final double maxTemperature;
    
    private final double humidity;

    @JsonCreator
    public MainPrediction(@JsonProperty("temp") double temperature, 
                                 @JsonProperty("temp_min") double minTemperature,
                                 @JsonProperty("temp_max")double maxTemperature, 
                                 @JsonProperty("humidity") double humidity)
    {
        this.temperature = temperature;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.humidity = humidity;
    }
    
    public double getHumidity()
    {
        return humidity;
    }

    public double getMaxTemperature()
    {
        return maxTemperature;
    }

    public double getMinTemperature()
    {
        return minTemperature;
    }

    public double getTemperature()
    {
        return temperature;
    }
    
}
