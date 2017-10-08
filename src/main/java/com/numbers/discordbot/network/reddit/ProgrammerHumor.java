package com.numbers.discordbot.network.reddit;

import com.google.inject.*;
import com.numbers.jttp.*;
import java.util.*;

public class ProgrammerHumor {

    private final List<ProgrammerPost> urls;

    @Inject
    public ProgrammerHumor(Jttp jttp)
    {
        urls = jttp.get("http://localhost:8080/reddit/programmerHumor").asObjects(List.class, ProgrammerPost.class).join().getResponse();
    }
    
    public List<ProgrammerPost> getPosts()
    {
        return urls;
    }
    
    public static class ProgrammerPost{
        private String url;
        private String title;

        public String getTitle()
        {
            return title;
        }

        public String getUrl()
        {
            return url;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }
    }
    
}
