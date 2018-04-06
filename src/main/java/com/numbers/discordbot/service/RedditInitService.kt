package com.numbers.discordbot.service

import com.numbers.discordbot.config
import net.dean.jraw.RedditClient
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper

class RedditInitService{

    val redditConfig = config["reddit"].asJsonObject
    val reddit: RedditClient

    init {
        val credentials = Credentials.script(redditConfig["username"].asString, redditConfig["password"].asString, redditConfig["client-id"].asString, redditConfig["secret"].asString)
        val userAgent = UserAgent("bot", "com.numbers.discordBot", "v0.0", redditConfig["username"].asString)
        val adapter = OkHttpNetworkAdapter(userAgent)
        reddit = OAuthHelper.automatic(adapter, credentials)
    }


}