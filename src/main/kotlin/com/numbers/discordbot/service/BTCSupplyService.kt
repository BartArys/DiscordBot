package com.numbers.discordbot.service

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.error
import kotlinx.coroutines.experimental.launch
import org.bson.types.ObjectId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import java.lang.reflect.Type
import java.time.LocalTime
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.suspendCoroutine

@Singleton
class BTCSupplyService @Inject constructor(private val client: IDiscordClient, private val dataService: BTCDataService, private val bitcoinService: BTCService, executorService: ScheduledExecutorService) : SupplyService<IChannel> {

    private companion object {
        var started = false
    }

    init {
        if (!started) {
            setTopic()

            executorService.scheduleAtFixedRate({
                setTopic()
            },  15 - (LocalTime.now().minute % 15).toLong(), 15, TimeUnit.MINUTES)

            started = true
        }

    }

    private fun setTopic() {
        bitcoinService.getInfo().enqueue(object : Callback<Map<String, BTCInfo>> {
            override fun onResponse(call: Call<Map<String, BTCInfo>>?, response: Response<Map<String, BTCInfo>>?) {
                response?.body()?.get("EUR")?.let { info ->
                    client.channels.forEach { channel ->
                        launch {
                            if(dataService.isSubscribed(channel)){
                                channel.changeTopic("BTC:  ${info.last}${info.symbol}")
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Map<String, BTCInfo>>?, t: Throwable?) {
                client.getOrCreatePMChannel(client.applicationOwner).sendMessage(t?.stackTrace?.joinToString("\n"))
            }

        })
    }

    override fun attachTo(target: IChannel) {
        if (!target.getModifiedPermissions(target.client.ourUser).contains(Permissions.MANAGE_CHANNELS)) {
            target.sendMessage(EmbedBuilder().error("missing permission: manage channels").build()).autoDelete()
            return
        }

        dataService.setSubscribed(target, true)
        setTopic()
    }

    override fun detachFrom(target: IChannel) {
        dataService.setSubscribed(target, false)
    }
}

data class BTCInfo(val previous: Double, val last: Double, val buy: Double, val sell: Double, val symbol: String)

class BTCInfoDeserializer : JsonDeserializer<Map<String, BTCInfo>> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Map<String, BTCInfo> {
        val info: MutableMap<String, BTCInfo> = mutableMapOf()
        json.asJsonObject.entrySet().forEach { entry -> info[entry.key] = deserializeBTCInfo(entry.value) }
        return info
    }

    private fun deserializeBTCInfo(json: JsonElement): BTCInfo {
        val response = json.asJsonObject
        return BTCInfo(response["15m"].asDouble, response["last"].asDouble, response["buy"].asDouble, response["sell"].asDouble, response["symbol"].asString)
    }

}

interface BTCService {

    @GET("https://blockchain.info/nl/ticker")
    fun getInfo(): Call<Map<String, BTCInfo>>

}

data class BTCSubscription(var id: ObjectId? = null, var channelId: String? = null, var subscribed: Boolean? = null)

class BTCDataService @Inject constructor(val db: MongoDatabase) {

    private val subscriptions: MongoCollection<BTCSubscription> = db.getCollection("BTCSubscriptions", BTCSubscription::class.java)

    companion object {
        val default = false
    }

    suspend fun isSubscribed(forChannel: IChannel) : Boolean =  suspendCoroutine {
        cont ->
        subscriptions.find(Filters.eq<String>("channelId", forChannel.stringID))
                .first { btcSubscription: BTCSubscription?, throwable: Throwable? ->
                    throwable?.let {
                        cont.resumeWithException(it)
                        return@first
                    }

                    cont.resume(btcSubscription?.subscribed ?: default)
                }
    }

    fun setSubscribed(forChannel: IChannel, subscribed: Boolean) {
        subscriptions.find(Filters.eq<String>("channelId", forChannel.stringID))
                .first { btcSubscription: BTCSubscription?, _ ->
                    if(btcSubscription != null){
                        subscriptions.replaceOne(Filters.eq<String>("channelId", forChannel.stringID), btcSubscription.copy(subscribed = subscribed), { _,_ -> })
                    }else{
                        subscriptions.insertOne(BTCSubscription(channelId = forChannel.stringID, subscribed = subscribed), { _,_ -> })
                    }
                }
    }
}