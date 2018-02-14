package com.numbers.discordbot.extensions

import com.numbers.discordbot.dsl.Book
import com.numbers.discordbot.dsl.bind
import com.numbers.discordbot.module.music.Track
import com.numbers.discordbot.module.music.format
import sx.blah.discord.util.EmbedBuilder

fun List<Track>.toSongSelectBook() : Book<List<String>>{
    return this.mapIndexed { index, it ->  it.format(false, index) }
            .weightedWindow(EmbedBuilder.DESCRIPTION_CONTENT_LIMIT) { length }
            .bind {
                description = item.joinToString("\n")
                footer {
                    text = "multiple tracks found, select by space separated numbers, 'all' or 'none'"
                    title = "info"
                }
            }
}