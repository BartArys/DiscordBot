package com.numbers.discordbot;

import com.google.inject.*;
import com.numbers.discordbot.audio.*;
import com.numbers.discordbot.client.*;
import com.numbers.discordbot.dependency.*;
import com.numbers.discordbot.filter.*;
import com.numbers.discordbot.loader.*;
import com.numbers.discordbot.persistence.*;
import java.lang.reflect.*;
import sx.blah.discord.api.*;
import sx.blah.discord.handle.obj.*;

public class Application {

    private static IDiscordClient client;
    
    public static void main(String[] args) throws Exception
    {
        MongoDB db = new MongoDB();
        Injector injector = Guice.createInjector(
                new CommandModule(db, new MusicManagerMap()),
                new PersistenceModule(db),
                new HttpModule(),
                new ConcurrencyModule()
        );
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.invisible();
            client.getConnectedVoiceChannels().forEach(IVoiceChannel::leave);
        }));
        
        EventListener el = new EventListener(injector);

        Class<?>[] classes = new CommandLoader().getClasses(
                "com.numbers.discordbot.commands");
        for (Class<?> cls : classes) {
            Constructor<?> ctr = cls.getConstructor();
            el.addCommand(ctr.newInstance());
        }

        client = Init.withToken().idle("shitty code simulator").login();

        client.getDispatcher().registerListener(el);
    }

}
