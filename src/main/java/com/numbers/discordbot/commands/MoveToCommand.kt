package com.numbers.discordbot.commands

import com.numbers.discordbot.Command
import com.numbers.discordbot.audio.MusicManagerCache
import com.numbers.discordbot.commands.util.*
import com.numbers.discordbot.filter.MessageFilter
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageTokenizer
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ScheduledExecutorService

@Command(name =  "Move Player")
class MoveToCommand {

    @Command
    @MessageFilter(eventType = MentionEvent::class, mentionsBot = true, regex = ".*moveTo\\s(\\d{2}:){0,2}\\d{2}", readableUsage = "moveTo \$hh:mm:ss")
    fun handle(event: MentionEvent, cache: MusicManagerCache,
               ses: ScheduledExecutorService) {
        val tokenizer = MessageTokenizer(event.message).skipNextMention().skipNextWord()
        moveTo(event, cache, ses, tokenizer)
    }

    @Command
    @MessageFilter(eventType = MessageEvent::class, prefixCheck = true, regex = "moveTo\\s(\\d{2}:){0,2}\\d{2}", readableUsage = "moveTo \$hh:mm:ss")
    fun handle(event: MessageEvent, cache: MusicManagerCache,
               ses: ScheduledExecutorService) {
        val tokenizer = MessageTokenizer(event.message).skipNextWord().skipNextWord()
        moveTo(event, cache, ses, tokenizer)
    }

    fun moveTo(event: MessageEvent, cache: MusicManagerCache, service: ScheduledExecutorService,  tokenizer: MessageTokenizer){
        val millies = (LocalTime.parse(tokenizer.nextWord().content, DateTimeFormatter.ISO_TIME).toSecondOfDay() * 1000).toLong()

        val gmm = cache.getGuildMusicManager(event.guild)

        val builder = EmbedBuilder()
        builder.withDefaultColor().withDeleteInfo()

        if(gmm.player.playingTrack != null){
            if(gmm.player.playingTrack.duration < millies) builder.appendDesc("given time would skip song :confused:")
            else{
                gmm.player.playingTrack.position = millies
                builder.appendDesc(gmm.player.playingTrack.infoString())
            }
        }else{
            builder.appendDesc("playlist is empty")
        }

        CommandUtil.delete(service, event.channel.sendMessage(builder.build()), event.message)
    }

}