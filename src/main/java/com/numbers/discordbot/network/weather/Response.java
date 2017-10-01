package com.numbers.discordbot.network.weather;

import com.fasterxml.jackson.annotation.*;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

    private final List<Prediction> list;

    @JsonCreator
    public Response(@JsonProperty("list")List<Prediction> predictions)
    {
        this.list = predictions;
    }
    
    public List<Prediction> getPredictions()
    {
        return list;
    }
    
}
