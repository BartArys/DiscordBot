package com.numbers.discordbot.filter;

import java.lang.annotation.*;
import sx.blah.discord.api.events.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MessageFilter {

    boolean mentionsBot() default false;
    String regex() default "";
    String startsWith() default "";
    Class<? extends Event> eventType();
}
