package com.numbers.discordbot.extensions

import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.module.music.SearchResultHandler
import com.numbers.discordbot.module.music.Track
import sx.blah.discord.handle.obj.IUser
import kotlin.coroutines.experimental.suspendCoroutine

suspend fun MusicPlayer.search(search: String, user: IUser) : Iterable<Track> = suspendCoroutine {
    cont ->

    this.search(search, user, object : SearchResultHandler{
        override fun onFailed(search: String, exception: Exception) {
            cont.resumeWithException(exception)
        }

        override fun onFindOne(search: String, track: Track) {
            cont.resume(kotlin.collections.listOf(track))
        }

        override fun onFindNone(search: String) {
            cont.resume(kotlin.collections.listOf())
        }

        override fun onFindMultiple(search: String, tracks: Iterable<Track>) {
            cont.resume(tracks)
        }

    })

}

fun MusicPlayer.add(tracks : Iterable<Track>) = tracks.forEach { this.add(it) }

