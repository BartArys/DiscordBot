package com.numbers.discordbot.dependency;

import com.fasterxml.jackson.databind.*;
import com.google.inject.*;
import com.numbers.jttp.*;
import com.numbers.jttp.mapper.*;
import java.io.*;
import java.util.*;

public class HttpModule extends AbstractModule{

    @Override
    protected void configure()
    {
        Jttp.JTTPConfig config = new Jttp.JTTPConfig();
        config.setJsonMapper(new JsonMapper() {
            final ObjectMapper mapper = new ObjectMapper();

            @Override
            public <T> T readValue(InputStream json, Class<T> valueClass)
            {
                try {
                    return mapper.readValue(json, valueClass);
                } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                    return null;
                }
            }

            @Override
            public <T, C extends Collection> C readValues(InputStream json,
                                                          Class<C> collectionClass,
                                                          Class<T> valueClass)
            {
                try {
                    return mapper.readValue(json, mapper.getTypeFactory().constructCollectionLikeType(collectionClass, valueClass));
                } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                    return null;
                }
            }

            @Override
            public <M extends Map<K, V>, K, V> Map<K, V> readValues(
                    InputStream json,
                    Class<M> mapClass,
                    Class<K> keyClass,
                    Class<V> valueClass)
            {
                try {
                    return mapper.readValue(json, mapper.getTypeFactory().constructMapLikeType(mapClass, keyClass, valueClass));
                } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                    return null;
                }
            }

            @Override
            public String writeValue(Object o)
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        Jttp jttp = Jttp.fromConfig(config);
        bind(Jttp.class).toInstance(jttp);
    }

}
