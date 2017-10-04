package com.numbers.discordbot.network.eightball;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

    private MagicResponse response;

    @JsonCreator
    public Response(@JsonProperty("magic")MagicResponse response)
    {
        this.response = response;
    }

    
    
    public MagicResponse getResponse()
    {
        return response;
    }
    
    
    
}
