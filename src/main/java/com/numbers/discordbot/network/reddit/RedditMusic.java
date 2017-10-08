package com.numbers.discordbot.network.reddit;

import com.google.inject.*;
import com.numbers.jttp.*;
import java.util.*;

public class RedditMusic {
    
    private final List<String> urls;

    @Inject
    public RedditMusic(Jttp jttp)
    {
        urls = jttp.get("http://localhost:8080/reddit/music").asObjects(List.class, String.class).join().getResponse();
    }
    
    public List<String> getUrls()
    {
        return urls;
    }
    
}
