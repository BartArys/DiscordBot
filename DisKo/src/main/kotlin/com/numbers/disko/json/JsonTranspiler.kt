package com.numbers.disko.json

import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import com.numbers.disko.commands
import com.numbers.disko.setup
import com.numbers.disko.registerTypeAdapter
import java.io.FileReader

fun JsonCommandContext.transpile() = JsonTranspiler.transpile(this)

class JsonTranspiler private constructor() {
    companion object Transpiler {

        private val commandGson = GsonBuilder()
                .registerTypeAdapter<JsonCommandContext>(JsonCommandContextDeserializer())
                .registerTypeAdapter<JsonGlobalSettings>(JsonGlobalSettingsDeserializer())
                .registerTypeAdapter<JsonCommands>(JsonCommandsDeserializer())
                .registerTypeAdapter<JsonCommand>(JsonCommandDeserialzer())
                .create()

        fun generateFromJson(uri: String) = commandGson.fromJson<JsonCommandContext>(JsonReader(FileReader(uri)), JsonCommandContext::class.java).transpile()

        fun transpile(context: JsonCommandContext) = setup {
            arguments {
                context.settings.tokens.forEach { forToken(it.key, it.value) }
                context.settings.arguments.forEach { forArgument(it.key, it.value) }
            }

            this + commands {
                context.commands.commands.forEach {
                    command(it.usage) {
                        arguments(*it.arguments.toTypedArray())

                        execute {
                            respond(container = it.response)
                        }
                    }
                }
            }
        }
    }
}