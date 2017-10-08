package com.numbers.discordbot;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.numbers.discordbot.audio.MusicManagerMap;
import com.numbers.discordbot.client.Init;
import com.numbers.discordbot.dependency.CommandModule;
import com.numbers.discordbot.dependency.ConcurrencyModule;
import com.numbers.discordbot.dependency.HttpModule;
import com.numbers.discordbot.dependency.PersistenceModule;
import com.numbers.discordbot.filter.MesageEventListener;
import com.numbers.discordbot.loader.CommandLoader;
import com.numbers.discordbot.persistence.MongoDB;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IVoiceChannel;

import java.lang.reflect.Constructor;

public class Application {

    public static void main(String[] args) throws Exception
    {
        IDiscordClient client = Init.withToken().idle("shitty code simulator").login();
        
        new ProcessBuilder(Init.mongoDbPath()).start();
        new ProcessBuilder(Init.nodeServerPath()).start();
        
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
        
        MesageEventListener mel = new MesageEventListener(injector);

        Class<?>[] classes = new CommandLoader().getClasses(
                Command.class,
                "com.numbers.discordbot.commands"
        );

        for (Class<?> cls : classes) {
            Constructor<?> ctr = cls.getConstructor();
            mel.addCommand(ctr.newInstance());
        }

        client.getDispatcher().registerListener(mel);
    }

}
