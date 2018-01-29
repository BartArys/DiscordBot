package com.numbers.discordbot.action

import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.service.BTCSupplyService
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer

class BTCListenAction {

    @Guards("""
        starts tracking the value of bitcoin in the channel's topic
        """",
            Guard("$ track bitcoin|btc|BTC"))
    fun track(message: IMessage, channel: IChannel, supplyService: BTCSupplyService){
        RequestBuffer.request { message.delete() }
        supplyService.attachTo(channel)
        EmbedBuilder.FIELD_CONTENT_LIMIT
    }

    @Guards("""
        stops tracking the value of bitcoin in the channel's topic
        """",
            Guard("$ untrack bitcoin|btc|BTC"))
    fun unTrack(message: IMessage, channel: IChannel, supplyService: BTCSupplyService){
        RequestBuffer.request { message.delete() }
        supplyService.detachFrom(channel)
    }

}