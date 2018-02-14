package com.numbers.discordbot.action

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.error
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.service.TagService
import kotlinx.coroutines.experimental.launch
import org.apache.commons.validator.routines.UrlValidator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer

class TagAction {

    private val dank = """```
 ________  ________  ________   ___  __            ________  ________           ________ ___  ___  ________  ___  __
|\   ___ \|\   __  \|\   ___  \|\  \|\  \         |\   __  \|\   ____\         |\  _____\\  \|\  \|\   ____\|\  \|\  \
\ \  \_|\ \ \  \|\  \ \  \\ \  \ \  \/  /|_       \ \  \|\  \ \  \___|_        \ \  \__/\ \  \\\  \ \  \___|\ \  \/  /|_
 \ \  \ \\ \ \   __  \ \  \\ \  \ \   ___  \       \ \   __  \ \_____  \        \ \   __\\ \  \\\  \ \  \    \ \   ___  \
  \ \  \_\\ \ \  \ \  \ \  \\ \  \ \  \\ \  \       \ \  \ \  \|____|\  \        \ \  \_| \ \  \\\  \ \  \____\ \  \\ \  \
   \ \_______\ \__\ \__\ \__\\ \__\ \__\\ \__\       \ \__\ \__\____\_\  \        \ \__\   \ \_______\ \_______\ \__\\ \__\
    \|_______|\|__|\|__|\|__| \|__|\|__| \|__|        \|__|\|__|\_________\        \|__|    \|_______|\|_______|\|__| \|__|
                                                               \|_________|
    ```""".trimIndent()

    @Guards(""""
        Displays a claimed tag
    """,
            Guard(":{tag}:", Argument(ArgumentType.WORD, "the name of the tag")))
    fun tag(event: MessageReceivedEvent, args: CommandArguments, service: TagService){
        launch {
            val tag = service.get(args["tag"]!!)
            if(tag == null){
                RequestBuffer.request { event.message.delete() }
                RequestBuffer.request { event.channel.sendMessage(EmbedBuilder().error().withDesc("tag is unclaimed").build()).autoDelete() }
                return@launch
            }
            RequestBuffer.request {
                if(UrlValidator.getInstance().isValid(tag)){
                    event.channel.sendMessage(EmbedBuilder().withImage(tag).build())
                }else{
                    event.channel.sendMessage(tag)
                }
            }
        }
    }

}