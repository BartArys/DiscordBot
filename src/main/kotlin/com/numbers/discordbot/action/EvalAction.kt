package com.numbers.discordbot.action

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.service.Permission
import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.MapContext
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RequestBuffer


class EvalAction {

    companion object {
        val jexl = JexlBuilder().create()!!
    }

    @Permissions(Permission.ADMIN)
    @Guard("$ eval {code}", description = "runs code in current context", params = [(Argument(ArgumentType.WORDS, "the code to evaluate"))])
    fun eval(event: MessageReceivedEvent, args: CommandArguments){
        val context = MapContext()
        context["event"] = event
        context["channel"] = event.channel
        context["author"] = event.author
        context["message"] = event.message

        try {
            val expression = jexl.createExpression(args.get<String>("code")!!.trim())
            expression.evaluate(context)
        }catch (ex: Exception){
            RequestBuffer.request { event.channel.sendMessage(ex.message).autoDelete() }
        }
    }
}