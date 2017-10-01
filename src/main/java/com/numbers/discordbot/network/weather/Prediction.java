package com.numbers.discordbot.network.weather;

import com.fasterxml.jackson.annotation.*;
import java.time.*;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Prediction {

    private final long isoDate;
    private final MainPrediction mainPrediction;
    private final List<WeatherPrediction> weatherPredictions;

    @JsonCreator
    public Prediction(@JsonProperty("dt")long isoDate, 
                      @JsonProperty("main") MainPrediction mainPrediction,
                      @JsonProperty("weather") List<WeatherPrediction> weatherPredictions)
    {
        this.isoDate = isoDate;
        this.mainPrediction = mainPrediction;
        this.weatherPredictions = weatherPredictions;
    }

    public MainPrediction getMainPrediction()
    {
        return mainPrediction;
    }

    public List<WeatherPrediction> getWeatherPredictions()
    {
        return weatherPredictions;
    }

    public long getIsoDate()
    {
        return isoDate;
    }

    @Override
    public String toString()
    {
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(isoDate, 0, ZoneOffset.ofHours(1));
        return String.format("%02d:%02d | temp %02.2f°C", ldt.getHour(), ldt.getMinute(), mainPrediction.getTemperature());
    }
    
    

}
