package com.numbers.discordbot.client;

import sx.blah.discord.api.*;

public class Init {

    public static ClientBuilder withToken(){
        return new ClientBuilder().withToken("MjQ5ODI2MzMyMTU2ODg3MDQw.DBlhuQ.yivVqDaejV-dHUo5zRflT8U3uVM");
    }
    
    public static String OpenWeatherKey(){
        return "bc1fe6b4b789099900652a34666ecbe4";
    }
    
    public static String mongoDbPath(){
        return "\"C:\\Program Files\\MongoDB\\Server\\3.4\\bin\\mongod.exe\"";
    }
    
    public static String[] nodeServerPath(){
        return  new String[]{"node","D:\\nodeProjects\\nodeRedditPlaylistService\\index.js"};
    }
}
