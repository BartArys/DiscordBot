package com.numbers.discordbot.action

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.network.EightBallResponse
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.EightBallService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RequestBuffer

class EightBallAction {

    @Guards("""
        ask a question to a high quality 8ball connected to a REST api
    """,
            Guard("$ 8ball {question}", Argument(ArgumentType.WORDS, "the question to ask")),
            Guard("$8ball {question}", Argument(ArgumentType.WORDS, "the question to ask"))
    )
    fun eightBall(event: MessageReceivedEvent, args: Map<String,String>, eightBallService: EightBallService, personality: Personality){
        eightBallService.shake(args["question"]!!).enqueue(object : Callback<EightBallResponse>{
            override fun onResponse(call: Call<EightBallResponse>, response: Response<EightBallResponse>) {
                event.message.autoDelete(120)
                RequestBuffer.request {
                    event.channel.sendMessage(personality.eightBall(response.body()!!).build()).autoDelete(120)
                }
            }

            override fun onFailure(call: Call<EightBallResponse>?, t: Throwable?) {
                RequestBuffer.request {
                    event.channel.sendMessage(personality.error(t!!).build()).autoDelete()
                }
            }

        })
    }
}