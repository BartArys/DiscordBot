package com.numbers.discordbot.service.discordservices

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import retrofit2.Call
import retrofit2.http.*
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser
import javax.inject.Inject

data class Reaction constructor(
        val id: Long = 0,
        var guild: String,
        var user: String,
        var key: String,
        var content: String
)

interface ReactionService {

    suspend fun getAllReactions(): List<Reaction> = getAllReactionsAsync().await()
    fun getAllReactionsAsync(): Deferred<List<Reaction>>

    suspend fun createReaction(user: IUser, guild: IGuild, key: String, content: String) = createReactionAsync(user, guild, key, content).await()
    fun createReactionAsync(user: IUser, guild: IGuild, key: String, content: String): Deferred<Unit>

    suspend fun getReactionsByKey(key: String): List<Reaction> = getReactionsByKeyAsync(key).await()
    fun getReactionsByKeyAsync(key: String): Deferred<List<Reaction>>

    suspend fun deleteReaction(reaction: Reaction) = deleteReactionAsync(reaction).await()
    fun deleteReactionAsync(reaction: Reaction): Deferred<Unit>
}

class InternalReactionService @Inject constructor(private val webService: ReactionWebService) : ReactionService {
    override fun getAllReactionsAsync(): Deferred<List<Reaction>> = async {
        webService.getAllReactions().execute().body().orEmpty()
    }

    override fun createReactionAsync(user: IUser, guild: IGuild, key: String, content: String): Deferred<Unit> = async {
        webService.addNewReaction(Reaction(user = user.stringID, guild = guild.stringID, key = key, content = content)).execute()
        return@async
    }

    override fun getReactionsByKeyAsync(key: String): Deferred<List<Reaction>> = async {
        webService.getReactionsByKey(key).execute().body()!!
    }

    override fun deleteReactionAsync(reaction: Reaction): Deferred<Unit> = async {
        webService.deleteReaction(reaction.id.toString()).execute().let {
            println(webService.deleteReaction(reaction.id.toString()).request().url())
            println(it.message())
        }
        return@async
    }

}

interface ReactionWebService {

    @GET("/reactions")
    fun getAllReactions(): Call<List<Reaction>>

    @POST("/reactions")
    fun addNewReaction(@Body reaction: Reaction): Call<Void>

    @PATCH("/reactions")
    fun updateReaction(@Body reaction: Reaction): Call<Void>

    @DELETE("/reactions/reaction/{id}")
    fun deleteReaction(@Path("id") reactionId: String): Call<Void>

    @GET("/reactions/user/{id}")
    fun getReactionByUser(@Path("id") userId: String): Call<List<Reaction>>

    @GET("/reactions/guild/{id}")
    fun getReactionByGuild(@Path("id") guildId: String): Call<List<Reaction>>

    @GET("/reactions/reaction/{id}")
    fun getReactionById(@Path("id") id: String): Call<Reaction>

    @GET("/reactions/key/{key}")
    fun getReactionsByKey(@Path("key") key: String): Call<List<Reaction>>

}