package com.numbers.discordbot.service

import com.google.inject.Provides
import com.google.inject.Singleton
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.WriteConcern
import com.mongodb.async.client.MongoClientSettings
import com.mongodb.async.client.MongoClients
import com.mongodb.async.client.MongoDatabase
import com.mongodb.connection.ClusterSettings
import com.numbers.discordbot.config
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider

@Singleton
class DBService {
    val mongoDatabase: MongoDatabase

    @Provides
    fun database() : MongoDatabase{
        return mongoDatabase
    }

    init {
        val pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClients.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder().automatic(true).build()
                )
        )

        /*
        val settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).clusterSettings(ClusterSettings.builder()
                .applyConnectionString(ConnectionString(config["mongodb"].asJsonObject["host"].asString))
                .build()).build()
        //val client = MongoClients.create(settings)

*/

        val mongoCredential = MongoCredential
                .createScramSha1Credential(config["mongodb"].asJsonObject["username"].asString, "discord",config["mongodb"].asJsonObject["password"].asString.toCharArray())

        val clusterSettings = ClusterSettings.builder()
                .hosts(listOf(ServerAddress(config["mongodb"].asJsonObject["host"].asString, config["mongodb"].asJsonObject["port"].asInt)))
                .build()

        val settings = MongoClientSettings.builder().clusterSettings(clusterSettings).codecRegistry(pojoCodecRegistry).credential(mongoCredential).build()
        val client = MongoClients.create(settings)


        mongoDatabase = client.getDatabase(config["mongodb"].asJsonObject["database"].asString).withWriteConcern(WriteConcern.ACKNOWLEDGED)
    }

    companion object {
        val database by lazy {
            DBService().database()
        }
    }
}