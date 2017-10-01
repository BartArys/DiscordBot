package com.numbers.discordbot.dependency;

import com.google.inject.*;
import java.util.concurrent.*;

public class ConcurrencyModule extends AbstractModule{

    @Override
    protected void configure()
    {
        bind(ScheduledExecutorService.class).toInstance(Executors.newSingleThreadScheduledExecutor());
    }

}
