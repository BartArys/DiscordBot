package com.numbers.discordbot.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId

data class Tag(var id: ObjectId? = null, var key: String? = null, var content: String? = null)

@Singleton
class TagService @Inject constructor(db: MongoDatabase)
    : AbstractDOptionalBService<Tag, String, String>(
        filter = { Filters.eq<String>("key", it) },
        mapper = { it.content!! },
        reverseMapper = { key, content -> Tag(null, key, content) }
) {
    override val collection: MongoCollection<Tag> = db.getCollection("tags", Tag::class.java)
}
