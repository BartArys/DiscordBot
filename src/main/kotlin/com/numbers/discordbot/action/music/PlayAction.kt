package com.numbers.discordbot.action.music

import com.numbers.discordbot.extensions.autoDelete
import com.numbers.discordbot.extensions.ensurePlayerCreated
import com.numbers.discordbot.extensions.toPaginatedMessage
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import com.numbers.discordbot.module.music.MusicPlayer
import com.numbers.discordbot.module.music.SearchResultHandler
import com.numbers.discordbot.module.music.Track
import com.numbers.discordbot.module.music.toEmbeds
import com.numbers.discordbot.permission.Permissions
import com.numbers.discordbot.personality.Personality
import com.numbers.discordbot.service.DisplayMessageService
import com.numbers.discordbot.service.Permission
import com.numbers.discordbot.service.SongSelectService
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.obj.Embed
import sx.blah.discord.util.RequestBuffer

class PlayAction {

    private val searchPrefix = "ytsearch:"

    @Permissions(Permission.MUSIC)
    @Guards(""""
        Plays the requested song. creates a player and joins a voice channel if required.

        Raises error when url or video is not supported.
    """,
            Guard("$ play|p {url}|{search terms}",
                    Argument(ArgumentType.URL, "the song url, YouTube, SoundCloud, Bandcamp, Vimeo and Twitch streams are supported"),
                    Argument(ArgumentType.WORDS, "tags to search youtube videos with")),
            Guard("\$p {url}|{search terms}",
                    Argument(ArgumentType.URL, "the song url, YouTube, SoundCloud, Bandcamp, Vimeo and Twitch streams are supported"),
                    Argument(ArgumentType.WORDS, "tags to search youtube videos with"))
    )
    fun play(event: MessageReceivedEvent, args: CommandArguments, personality: Personality, service: DisplayMessageService, songSelectService: SongSelectService, player: MusicPlayer){
        event.ensurePlayerCreated(service, player)
        val search = args["url"] ?: "$searchPrefix${args.get<String>("search terms")}"
        RequestBuffer.request { event.channel.typingStatus = true }

        player.search(search, event.author, object: SearchResultHandler{
            override fun onFailed(search: String, exception: Exception) {
                event.message.autoDelete()
                RequestBuffer.request { event.channel.sendMessage(personality.songLoadFailed(exception).build()) }
            }

            override fun onFindOne(search: String, track: Track) {
                RequestBuffer.request { event.message.delete() }
                event.channel.sendMessage(personality.songFound(track).build()).autoDelete()
                player.add(track)
            }

            override fun onFindNone(search: String) {
                event.message.autoDelete()
                event.channel.sendMessage(personality.noMatches(false).build()).autoDelete()
            }

            override fun onFindMultiple(search: String, tracks: Iterable<Track>) {
                RequestBuffer.request { event.message.delete() }
                if(args.get<Any>("url") != null){
                    event.channel.sendMessage(personality.playlistFound(tracks).build()).autoDelete()
                    tracks.forEach { player.add(it) }
                }else{
                    val message =event.channel.sendMessage(personality.selectSong(tracks).build())
                    val paginated = tracks.toList().toEmbeds()
                            .toPaginatedMessage(
                                    message,
                                    footer =  Embed.EmbedField("info:", "multiple tracks found, select by space separated numbers, 'all' or 'none'", true)) { songSelectService.deleteFor(event.author, event.channel) }
                    event.client.dispatcher.registerListener(paginated)
                    songSelectService.setFor(event.author, event.channel, tracks = tracks.toList(), message = message)
                }
            }

        })
    }

}