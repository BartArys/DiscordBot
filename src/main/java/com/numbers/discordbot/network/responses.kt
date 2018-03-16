package com.numbers.discordbot.network

import com.google.gson.annotations.JsonAdapter
import com.numbers.discordbot.network.deserializer.EightBallResponseDeserializer

@JsonAdapter(EightBallResponseDeserializer::class)
data class EightBallResponse(val question: String, val answer: String, val type: String)
