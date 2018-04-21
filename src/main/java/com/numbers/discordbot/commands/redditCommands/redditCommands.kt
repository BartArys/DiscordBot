package com.numbers.discordbot.commands.redditCommands

import com.numbers.discordbot.dsl.CommandsSupplier
import com.numbers.discordbot.dsl.commands
import com.numbers.discordbot.dsl.guard.canSendMessage
import com.numbers.discordbot.dsl.gui.builder.Emote
import com.numbers.discordbot.dsl.gui2.ScreenBuilder
import com.numbers.discordbot.dsl.gui2.controls
import com.numbers.discordbot.dsl.gui2.deletable
import com.numbers.discordbot.extensions.random
import net.dean.jraw.RedditClient
import net.dean.jraw.models.Submission
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.models.TimePeriod
import net.dean.jraw.pagination.DefaultPaginator

@CommandsSupplier
fun redditCommands() = commands {

    simpleCommand("£ joke", { canSendMessage } ){
        val joke = services<RedditClient>().subreddit("Jokes").posts().sorting(SubredditSort.TOP).timePeriod(TimePeriod.WEEK).limit(50).build().first().random()

        respond {
            title = joke.title
            url = joke.url
            description =  if(joke.selfText.orEmpty().length >= 2000){
                joke.selfText?.substring(0,2000) + "..."
            }else joke.selfText

        }
    }

    simpleCommand("£ embed joke", { canSendMessage } ){
        respond.screen (block = RedditPostNavigator(services<RedditClient>().subreddit("Jokes").posts().sorting(SubredditSort.TOP).timePeriod(TimePeriod.WEEK).limit(50).build()).asScreen())
    }

    simpleCommand("£ embed dank", { canSendMessage } ){
        respond.screen (block = RedditPostNavigator(services<RedditClient>().subreddit("DankMemes").posts().sorting(SubredditSort.TOP).timePeriod(TimePeriod.WEEK).limit(50).build()).asScreen())
    }


}

class RedditPostNavigator(private val pagination: DefaultPaginator<Submission>){
    fun asScreen() : ScreenBuilder.() -> Unit = {
        property(deletable)

        pagination.next()
        var postIndex = 0

        fun currentPage() = pagination.current?.get(postIndex)
        fun isPageEnd(): Boolean = postIndex >= pagination.current?.size ?: 0

        onRefresh {
            title = currentPage()?.title ?: "no more posts"
            url = currentPage()?.url
            description = if(currentPage()?.selfText.orEmpty().length >= 2000){
                currentPage()?.selfText?.substring(0,2000) + "..."
            }else currentPage()?.selfText
        }

        controls {
            forEmote(Emote.next) { screen, _ ->
                if(isPageEnd()) {
                    pagination.next()
                    postIndex = 0
                }else postIndex++

                screen.refresh()
            }
        }
    }
}