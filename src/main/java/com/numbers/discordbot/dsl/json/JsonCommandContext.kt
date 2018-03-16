package com.numbers.discordbot.dsl.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.numbers.discordbot.dsl.*
import java.lang.reflect.Type

data class JsonCommandContext(val settings: JsonGlobalSettings, val commands: JsonCommands)

data class JsonGlobalSettings(val tokens : Map<Char, Argument> = mapOf(), val arguments: Map<String, Argument> = mapOf()){
    companion object { const val key = "settings" }
}

data class JsonCommands(val commands : List<JsonCommand> = listOf()){
    companion object { const val key = "commands" }
}

data class JsonCommand(
        var usage: String = "",
        var arguments: List<Argument> = emptyList(),
        var settings: JsonCommandSettings = JsonCommandSettings(),
        var response: EmbedContainer.() -> Unit = {}
)

data class JsonCommandSettings(var literal: Boolean = false, var ignoreCase : Boolean = false)

class JsonCommandContextDeserializer : JsonDeserializer<JsonCommandContext>{

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): JsonCommandContext {
        val settings = json.asJsonObject[JsonGlobalSettings.key]
                ?.let { context.deserialize<JsonGlobalSettings>(it, JsonGlobalSettings::class.java) }
                ?: JsonGlobalSettings()

        val commands = json.asJsonObject[JsonCommands.key]
                ?.let { context.deserialize<JsonCommands>(it, JsonCommands::class.java) }
                ?: JsonCommands()

        return JsonCommandContext(settings, commands)
    }
}

class JsonGlobalSettingsDeserializer : JsonDeserializer<JsonGlobalSettings> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): JsonGlobalSettings {
        val tokens = json.asJsonObject["items"]
                ?.asJsonObject
                ?.entrySet()?.map { it.key[0] to it.value.asString.asArgument(it.key) }?.toMap() ?: mapOf()

        val arguments = json.asJsonObject["arguments"]
                ?.asJsonObject
                ?.entrySet()?.map { it.key to it.value.asString.asArgument(it.key) }?.toMap() ?: mapOf()

        return JsonGlobalSettings(tokens, arguments)
    }

}

private fun String.asArgument(key: String) : Argument = when(this){
    "words" -> words(key)
    "word" -> word(key)
    "prefix" -> prefix
    "user" -> userMention(key)
    else -> TODO("add more argument keys")
}

class JsonCommandsDeserializer : JsonDeserializer<JsonCommands>{
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): JsonCommands {
        val commands = json.asJsonObject.entrySet().map { entry ->
            context.deserialize<JsonCommand>(entry.value, JsonCommand::class.java)
                    .also { it.usage = entry.key }
        }

        return JsonCommands(commands)
    }
}

class JsonCommandDeserialzer : JsonDeserializer<JsonCommand>{
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): JsonCommand {
        return when {
            json.isJsonPrimitive -> JsonCommand { description = json.asString }
            json.isJsonArray -> {
                val array = deserializeArray(json)
                JsonCommand { description = array.next() }
            }
            else -> deserializeFullJsonCommand(json.asJsonObject)
        }
    }

    private fun deserializeFullJsonCommand(json: JsonObject) : JsonCommand {
        val settings = json["settings"]?.asJsonObject?.let {
            val literal = it.get("literal")?.asBoolean ?: false
            val ignoreCase = it.get("ignoreCase")?.asBoolean ?: false
            JsonCommandSettings(literal, ignoreCase)
        } ?: JsonCommandSettings()

        val arguments = json["arguments"]?.asJsonArray?.flatMap {
            it.asJsonObject.entrySet()
        }?.map { it.value.asString.asArgument(it.key) } ?: emptyList()

        val response = json["response"]

        val answers = when {
            response.isJsonArray -> { deserializeArray(response.asJsonArray) }
            else -> {
                RandomCollection<String>().also { it.add(1.0 ,response.asJsonObject.get("description")?.asString ?: "") }
            }
        }

        return JsonCommand(settings = settings, arguments = arguments) {
            description = answers.next()
        }
    }

    private fun deserializeStringArray(json: JsonElement) : RandomCollection<String>{
        val collection = RandomCollection<String>()
        json.asJsonArray.map { it.asString }.forEach {  collection.add(1.0, it) }
        return collection
    }

    private fun deserializeWeightedArray(json: JsonElement) : RandomCollection<String> {
        val collection = RandomCollection<String>()
        json.asJsonArray.flatMap { it.asJsonObject.entrySet() }.forEach {  collection.add(it.value.asDouble, it.key) }
        return collection
    }
    private fun deserializeArray(json: JsonElement) : RandomCollection<String> {
        return if(json.asJsonArray.first().isJsonObject) deserializeWeightedArray(json)
        else deserializeStringArray(json)
    }

}