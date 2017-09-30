package com.numbers.discordbot;

import com.numbers.discordbot.audio.*;
import com.numbers.discordbot.client.*;
import com.numbers.discordbot.filter.*;
import com.numbers.discordbot.loader.*;
import java.lang.reflect.*;
import sx.blah.discord.api.*;
import sx.blah.discord.api.events.*;
import sx.blah.discord.handle.obj.*;

public class Application {

    private static IDiscordClient client;

    public static void main(String[] args) throws Exception
    {
        Audio.Init();
        EventListener el = new EventListener();

        Class<IListener>[] classes = new ListenerLoader().getClasses(
                "com.numbers.ballbasherbot.commands");
        for (Class<IListener> cls : classes) {
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
