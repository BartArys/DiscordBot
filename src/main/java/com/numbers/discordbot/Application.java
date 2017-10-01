package com.numbers.discordbot;

import com.mongodb.client.*;
import com.numbers.discordbot.audio.*;
import com.numbers.discordbot.client.*;
import com.numbers.discordbot.filter.*;
import com.numbers.discordbot.loader.*;
import com.numbers.discordbot.persistence.*;
import java.lang.reflect.*;
import javax.swing.text.*;
import sx.blah.discord.api.*;
import sx.blah.discord.api.events.*;
import sx.blah.discord.handle.obj.*;

public class Application {

    private static IDiscordClient client;
    private static MongoDB mongoDB;
    
    public static void main(String[] args) throws Exception
    {
        mongoDB = new MongoDB();
        Audio.Init();
        EventListener el = new EventListener();

        Class<IListener>[] classes = new ListenerLoader().getClasses(
                "com.numbers.discordbot.commands");
        for (Class<IListener> cls : classes) {
            System.out.println(cls.getName());
            Constructor<IListener> ctr = cls.getConstructor();
            el.addListener(ctr.newInstance());
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.invisible();
            client.getConnectedVoiceChannels().forEach(IVoiceChannel::leave);
        }));

        client = Init.withToken().idle("shitty code simulator").login();
        client.getDispatcher().registerListener(el);
    }

}
