package com.numbers.discordbot.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.numbers.discordbot.module.astolfo.AstolfoPersonality
import com.numbers.discordbot.personality.DefaultPersonality
import com.numbers.discordbot.personality.DeusVultPersonality
import com.numbers.discordbot.personality.Personality
import org.bson.types.ObjectId
import sx.blah.discord.handle.obj.IUser

@Singleton
class PersonalityManager @Inject constructor(private val service: PersonalityService){

    companion object {
        enum class Personalities{
            DEUS,
            DEFAULT,
            ASTOLFO
        }
    }


    suspend fun forUser(user: IUser) : Personality{
        return when(service.get(user)){
            Personalities.DEFAULT.name.toLowerCase() -> DefaultPersonality()
            Personalities.DEUS.name.toLowerCase() -> DeusVultPersonality()
            Personalities.ASTOLFO.name.toLowerCase() -> AstolfoPersonality()
            else -> DefaultPersonality()
        }
    }

    fun setForUser(user: IUser, personality: Personalities) {
        service.set(user, personality.name.toLowerCase())
    }



}


@Singleton
class PersonalityService @Inject constructor(db: MongoDatabase)
    : AbstractDBService<PersonalityService.Personality, String, IUser>(
        default = Personality(null, null, "default"),
        filter = { Filters.eq<String>("userId", it.stringID) },
        mapper = { it.personality!! },
        reverseMapper = { user,value -> Personality(null, user.stringID, value) }) {

    data class Personality(var id: ObjectId? = null, var userId: String? = null, var personality: String? = null)

    override val collection: MongoCollection<Personality> = db.getCollection("persona", Personality::class.java)


}