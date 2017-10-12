package com.numbers.discordbot.persistence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.numbers.discordbot.persistence.entities.WednesDayCheck;

import java.util.Optional;

public class WednesDayRepository {

    private final MongoCollection<WednesDayCheck> wednesDayChecks;

    public WednesDayRepository(MongoDatabase database) {
        wednesDayChecks = database
                .getCollection("wednesday", WednesDayCheck.class);
    }

    public Optional<WednesDayCheck> getWednesDayCheck() {
        WednesDayCheck check = wednesDayChecks.find().first();
        return Optional.ofNullable(check);
    }

    public void put(WednesDayCheck check) {
        wednesDayChecks.insertOne(check);
    }

    public void update(WednesDayCheck check) {
        wednesDayChecks.replaceOne(Filters.eq("id", check.getObjectId()),
                check);
    }
}
