package com.numbers.discordbot.service.discordservices

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.numbers.discordbot.service.RxOkHttpWebSocket
import com.numbers.discordbot.service.RxWebSocket
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import sx.blah.discord.handle.obj.IUser
import java.lang.reflect.Type

@JsonAdapter( PrefixJsonMapper::class)
data class Prefix(val userId: String, val prefix: String)

interface PrefixService{

    fun getPrefix(user: IUser) : String

    fun setPrefix(user: IUser, prefix: String)

    fun reconnect()
}

internal class InternalPrefixService(private val webService: PrefixWebService, gson: Gson, wsUrl : String) : PrefixService{

    private val client = OkHttpClient.Builder().retryOnConnectionFailure(true).build()
    private val socket: RxWebSocket<Prefix> = RxOkHttpWebSocket(client, Request.Builder().url(wsUrl).build(), gson, Prefix::class.java)

    private val cache : MutableMap<String, String> = mutableMapOf()

    init {
        socket.value.subscribe { cache[it.userId] = it.prefix }

        webService.getAllPrefixes().execute().body().orEmpty().forEach {
            if(!cache.containsValue(it.userId)) cache[it.userId] = it.prefix
        }
    }

    override fun getPrefix(user: IUser): String {
        return cache[user.stringID] ?: "b;"
    }

    override fun setPrefix(user: IUser, prefix: String) {
        webService.setPrefix(Prefix(user.stringID, prefix)).execute()
    }

    override fun reconnect() = socket.reconnect()
}

interface PrefixWebService {

    @GET("/prefixes")
    fun getAllPrefixes() : Call<List<Prefix>>

    @GET("/prefixes/user/{user-id}")
    fun getPrefix(@Path("user-id") forUserId : String) : Call<Prefix?>

    @POST("/prefixes")
    fun setPrefix(@Body prefix: Prefix) : Call<Prefix>

}

class PrefixJsonMapper : JsonDeserializer<Prefix>, JsonSerializer<Prefix>{

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Prefix {
        val json = json.asJsonObject

        val userId = json["user"].asString
        val prefix = json["prefix"].asString

        return Prefix(userId, prefix)
    }

    override fun serialize(src: Prefix, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonObject().also {
            it.addProperty("user", src.userId)
            it.addProperty("prefix", src.prefix)
        }
    }
}
