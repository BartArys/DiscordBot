package com.numbers.discordbot.action

import com.google.inject.Singleton
import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.error
import com.numbers.discordbot.extensions.sendMessageAsync
import com.numbers.discordbot.extensions.toPaginatedMessage
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.WikiSearchService
import com.numbers.discordbot.service.toEmbeds
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import java.io.IOException

@Singleton
class WikiSearchAction {

    @Guards("""
        searches wikipedia for info.
    """,Guard("$ wiki {search}", Argument(ArgumentType.WORDS, "the content to search for")))
    fun wiki(message: IMessage, channel: IChannel, client: IDiscordClient, args: CommandArguments, wikiSearchService: WikiSearchService, personality: Personality){
        launch {
            val msg = channel.sendMessageAsync(EmbedBuilder().withDesc("using highly advanced wiki search algorithms...").build())

            val response = try {
                wikiSearchService.searchFor(search = args["search"]!!).execute()
            }catch (ex : IOException){
                message.autoDelete()
                RequestBuffer.request { msg.edit(personality.error(ex).build()) }
                return@launch
            }

            response.errorBody()?.let {
                message.autoDelete()
                RequestBuffer.request { msg.edit(EmbedBuilder().error(it.string()).build()) }
                return@launch
            }

            RequestBuffer.request { message.delete() }

            val paginated = response.body()!!.items.toEmbeds().toPaginatedMessage(message = msg)
            client.dispatcher.registerListener(paginated)
        }
    }

}