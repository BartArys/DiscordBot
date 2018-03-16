package com.numbers.discordbot.network.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.numbers.discordbot.network.EightBallResponse
import java.lang.reflect.Type

class EightBallResponseDeserializer : JsonDeserializer<EightBallResponse> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EightBallResponse {
        val magic = json.asJsonObject["magic"].asJsonObject

        return EightBallResponse(question = magic["question"].asString, answer = magic["answer"].asString, type = magic["type"].asString)
    }
}