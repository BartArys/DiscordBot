package com.numbers.discordbot.network.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.numbers.discordbot.network.DiscordEmojiResponse
import java.lang.reflect.Type

class DiscordEmojiDeserializer : JsonDeserializer<DiscordEmojiResponse> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DiscordEmojiResponse {
        val response = json.asJsonObject

        return DiscordEmojiResponse(
                id = response["id"].asString,
                category = response["category"].asLong,
                description = response["description"].asString,
                slug = response["slug"].asString,
                submittedBy = response["submittedBy"].asString,
                title = response["title"].asString
        )
    }
}

class DiscordEmojisDeserializer : JsonDeserializer<List<DiscordEmojiResponse>> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List<DiscordEmojiResponse> {
        return json.asJsonArray.map { context.deserialize<DiscordEmojiResponse>(it, DiscordEmojiResponse::class.java) }
    }


}