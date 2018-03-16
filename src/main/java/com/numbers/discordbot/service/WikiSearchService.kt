package com.numbers.discordbot.service

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
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

@JsonAdapter(WikiSearchDeserializer::class)
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

