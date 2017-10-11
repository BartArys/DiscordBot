package com.numbers.discordbot.filter;

import sx.blah.discord.api.events.Event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MessageFilter {

    boolean mentionsBot() default false;
    String regex() default "";
    String readableUsage();
    String startsWith() default "";
    Class<? extends Event> eventType();
    boolean prefixCheck() default false;
}
