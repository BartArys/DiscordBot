package com.numbers.discordbot.commands;

import com.numbers.discordbot.Command;
import com.numbers.discordbot.client.Init;
import com.numbers.discordbot.filter.MessageFilter;
import com.numbers.discordbot.network.weather.Prediction;
import com.numbers.discordbot.network.weather.Response;
import com.numbers.discordbot.persistence.WeatherRepository;
import com.numbers.discordbot.persistence.entities.WeatherPreference;
import com.numbers.jttp.Jttp;
import com.numbers.jttp.response.JsonHttpResponse;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageTokenizer;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Command
public class WeatherCommand {

    @Command
    @MessageFilter(eventType = MentionEvent.class, mentionsBot = true,
            regex = ".*\\sweather(\\s.*)?")
    public void handle(MentionEvent event, WeatherRepository repository,
                       Jttp jttp)
    {
        MessageTokenizer tokenizer = new MessageTokenizer(event.getMessage());
        tokenizer.nextMention(); //bot
        tokenizer.nextWord(); //weather

        if (tokenizer.hasNext()) {
            WeatherPreference pref = WeatherPreference.parseFrom(tokenizer
                    .getRemainingContent());
            if (pref == null) {
                event.getChannel().sendMessage(
                        "wrong formatting, expected '[cityName],[CountryCode]'");
                return;
            }
            doWeather(pref, event, jttp);
        } else {
            Optional<WeatherPreference> optPref = repository
                    .getPreferenceFromUser(event.getAuthor());

            if (!optPref.isPresent()) {
                event.getChannel().sendMessage(
                        "wrong formatting, expected '[cityName],[CountryCode]'");
                return;
            }

            doWeather(optPref.get(), event, jttp);
        }

    }

    private void doWeather(WeatherPreference preference, MessageEvent event,
                           Jttp jttp)
    {
        long beforeMS = System.currentTimeMillis();
        JsonHttpResponse<Response> httpResponse
                = jttp.get("http://api.openweathermap.org/data/2.5/forecast")
                .queryString("units", "metric")
                .queryString("appid", Init.OpenWeatherKey())
                .queryString("q", String.format("%s,%s", preference.getCity(),
                        preference.getCountry()))
                .asObject(Response.class)
                .join();

        if (!httpResponse.isSuccess()) {
            event.getChannel().sendMessage(
                    ":sad: something went wrong in the request");
            return;
        }

        Response response = httpResponse.getResponse();
        Prediction prediction = response.getPredictions().get(0);

        long afterMs = System.currentTimeMillis();
        EmbedObject eo = new EmbedBuilder()
                .withAuthorName("information provided by Openweather")
                .withAuthorUrl("http://openweathermap.org/").withColor(
                Color.BLUE)
                .withTitle(String.format("Weather forecast %s, %s", preference
                        .getCity(), preference.getCountry()))
                .withFooterText(
                        "this message took " + (afterMs - beforeMS) + "ms to create")
                .withFooterIcon(
                        "http://dl.hiapphere.com/data/icon/201604/HiAppHere_com_org.mokee.openweathermapprovider.png")
                .withTimestamp(LocalDateTime.now())
                .withDesc(prediction.toString()).build();

        event.getChannel().sendMessage(eo);
    }

}
