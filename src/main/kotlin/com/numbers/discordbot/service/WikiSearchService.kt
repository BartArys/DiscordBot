package com.numbers.discordbot.service

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.obj.Embed
import sx.blah.discord.util.EmbedBuilder
import java.lang.reflect.Type

interface WikiSearchService {

    @GET("https://en.wikipedia.org/w/api.php")
    fun searchFor(
            @Query("action") action: String = "opensearch",
            @Query("search") search : String,
            @Query("limit") limit : Int = 10,
            @Query("format") format : String = "json") : Call<WikiSearchResult>

}

data class WikiSearchItem(val title : String, val url : String, val description: String)

data class WikiSearchResult(val items : List<WikiSearchItem>)

class WikiSearchDeserializer : JsonDeserializer<WikiSearchResult> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): WikiSearchResult {
        val titles = json.asJsonArray[1].asJsonArray
        val descriptions = json.asJsonArray[2].asJsonArray
        val urls = json.asJsonArray[3].asJsonArray

        val list = (0 until titles.size()).map { WikiSearchItem(titles[it].asString, urls[it].asString, descriptions[it].asString) }
        return WikiSearchResult(list)
    }

}

fun List<WikiSearchItem>.toEmbeds() : List<Embed.EmbedField> {
    return this.map { Embed.EmbedField("${it.title.padEnd(1, '_')} - ${it.url}",it.description.padEnd(1, '_'), false) }
}

fun List<WikiSearchItem>.toDisplay() : EmbedObject {

    val builder = EmbedBuilder()

    this.map { Embed.EmbedField("${it.title.padEnd(1, '_')} - ${it.url}",it.description.padEnd(1, '_'), false) }
            .forEach { builder.appendField(it) }

    return builder.build()
}