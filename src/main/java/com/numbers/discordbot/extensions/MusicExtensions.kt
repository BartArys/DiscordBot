package com.numbers.discordbot.extensions

import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.module.music.SearchResultHandler
import com.numbers.discordbot.module.music.Track
import sx.blah.discord.handle.obj.IUser
import kotlin.coroutines.experimental.suspendCoroutine

suspend fun MusicPlayer.search(search: String, user: IUser): SearchResult = suspendCoroutine { cont ->

    this.search(search, user, object : SearchResultHandler {
        override fun onFailed(search: String, exception: Exception) {
            cont.resumeWithException(exception)
        }

        override fun onFindOne(search: String, track: Track) {
            cont.resume(Single(track))
        }

        override fun onFindNone(search: String) {
            cont.resume(Empty())
        }

        override fun onFindMultiple(search: String, tracks: Iterable<Track>) {
            cont.resume(Multiple(*tracks.toList().toTypedArray()))
        }

    })

}

fun MusicPlayer.add(tracks: Iterable<Track>) = tracks.forEach { this.add(it) }


sealed class SearchResult
class Empty : SearchResult()
class Single(val track: Track) : SearchResult(), Track by track
class Multiple(vararg val tracks: Track) : SearchResult()