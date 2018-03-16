package com.numbers.discordbot.message

import com.numbers.discordbot.extensions.then
import com.numbers.discordbot.module.music.*
import com.vdurmont.emoji.EmojiManager
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.handle.impl.obj.Embed
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.RequestBuilder
import java.time.Duration

class MusicPlayerMessage(private val musicPlayer : MusicPlayer, override val message: IMessage, private val onDelete: () -> Unit = {}) : RoundRobinPaginatedMessage, MusicEventListener{

    override val pageCount: Int get() {
        var ret = musicPlayer.scheduler.tracks.size / 5
        if(musicPlayer.scheduler.tracks.size % 5 != 0) ret++

        return ret
    }

    override var page: Int = 0

    override val needsRefresh: Boolean
        get() = !musicPlayer.isPaused

    override fun init(){
        musicPlayer.eventListeners += this

        val builder = RequestBuilder(message.client).shouldBufferRequests(true)

        builder.doAction { true }

        Emoji.values().sortedBy { it.order }.map { it.unicode }.forEach {
            builder.then {
                EmojiManager.getByUnicode(it)?.let { message.addReaction(it) }
            }
        }

        VolumeLevel.values().sortedBy { it.volume }.map { it.unicode }.forEach {
            builder.then {
                message.addReaction(EmojiManager.getByUnicode(it))
            }
        }

        builder.build()

        super.init()
    }

    override fun close() {
        this.message.guild.connectedVoiceChannel?.leave()
        this.musicPlayer.skipAll()
        super.close()
        onDelete()
    }

    override fun refresh() {
        if(message.isDeleted) return

        val currentSong = Embed.EmbedField(
                if(!musicPlayer.isPaused) "\uD83C\uDFB5 Currently playing" else "Currently paused",
                musicPlayer.currentTrack?.format(true) ?: "-",
                true)

        val songWindow = musicPlayer.scheduler.tracks.filter { it != musicPlayer.scheduler.current }.windowed(5,5, true)
        val songs = if(page > songWindow.size-1) {
            songWindow.lastOrNull() ?: emptyList()
        }else{
            songWindow[page]
        }

        val songEmbeds = songs.toEmbeds(musicPlayer.scheduler.tracks.indexOf(songs.firstOrNull() ?: 0))

        val statusVolume = Embed.EmbedField("Volume" , "$volume", true)

        val statusPage = Embed.EmbedField("Page", "${page +1}/$pageCount", true)

        val builder = EmbedBuilder().appendField(currentSong)
        songEmbeds.forEach { builder.appendField(it) }
        builder.appendField(statusVolume).appendField(statusPage)

        message.edit(builder.build())
    }

    var volume : Int
        get() { return musicPlayer.volume }
        set(value) {
            musicPlayer.volume = value
            RequestBuffer.request{ refresh() }
        }

    fun togglePause(){
        musicPlayer.isPaused = !musicPlayer.isPaused
        RequestBuffer.request{ refresh() }
    }

    fun stop(){
        musicPlayer.skipAll()
        RequestBuffer.request{ refresh() }
    }

    fun shuffle(){
        musicPlayer.scheduler.shuffle()
        RequestBuffer.request{ refresh() }
    }

    fun repeat(){
        musicPlayer.scheduler = RepeatListScheduler()
        RequestBuffer.request{ refresh() }
    }

    fun toStart(){
        musicPlayer.seak(Duration.ZERO)
        RequestBuffer.request{ refresh() }
    }

    fun toEnd(){
        musicPlayer.currentTrack?.let {
            musicPlayer.seak(it.duration)
        }
        RequestBuffer.request{ refresh() }
    }

    fun nextSong(){
        musicPlayer.skip()
        RequestBuffer.request{ refresh() }
    }

    fun previousSong(){
        // TODO
        RequestBuffer.request{ refresh() }
    }

    override fun handle(event: ReactionAddEvent) {
        if(message.isDeleted) return
        if(event.user. stringID == event.client.ourUser.stringID)  return
        if(event.message.stringID != message.stringID) return

        val name = event.reaction.emoji?.name

        when(name){
            Emoji.PLAY_PAUSE.unicode -> togglePause()
            Emoji.STOP.unicode -> stop()
            Emoji.SHUFFLE.unicode -> shuffle()
            Emoji.FORWARD.unicode -> nextSong()
            Emoji.BACKWARD.unicode -> previousSong()
            Emoji.REPEAT.unicode -> repeat()
            Emoji.REWIND.unicode -> toStart()
            Emoji.FAST_FORWARD.unicode -> toEnd()
            VolumeLevel.HIGH.unicode ->  volume = VolumeLevel.HIGH.volume
            VolumeLevel.MEDIUM.unicode -> volume = VolumeLevel.MEDIUM.volume
            VolumeLevel.LOW.unicode -> volume = VolumeLevel.LOW.volume
        }

        super.handle(event)
    }

    enum class Emoji(val order : Int, val unicode : String){
        PLAY_PAUSE(0,"\u23EF"),
        STOP(1,"\u23F9"),
        REWIND(2,"\u23EA"),
        FAST_FORWARD(3,"\u23E9"),
        FORWARD(4,"\u23ED\uFE0F"),
        BACKWARD(5,"\u23EE\uFE0F"),
        SHUFFLE(6, "\uD83D\uDD00"),
        REPEAT(7, "\uD83D\uDD01"),
        PREVIOUS_PAGE(8,"\u25C0"),
        NEXT_PAGE(9, "\u25B6")
    }

    enum class VolumeLevel(val volume : Int, val unicode: String){

        LOW(50, "\uD83D\uDD08"),
        MEDIUM(100, "\uD83D\uDD09"),
        HIGH(150,"\uD83D\uDD0A"),

    }

    override fun onTrackStart(player: MusicPlayer, track: Track) {
        RequestBuffer.request{ refresh() }
    }

    override fun onTrackPause(player: MusicPlayer) {
        RequestBuffer.request{ refresh() }
    }

    override fun onTrackResume(player: MusicPlayer) {
        RequestBuffer.request{ refresh() }
    }

    override fun onTrackEnd(player: MusicPlayer, track: Track, reason: TrackEndReason) {
        RequestBuffer.request{ refresh() }
    }

    override fun onTrackException(player: MusicPlayer, track: Track, exception: Exception) {
        RequestBuffer.request{ refresh() }
    }

    override fun onTrackStuck(player: MusicPlayer, track: Track, durationStuck: Duration) {
        RequestBuffer.request{ refresh() }
    }

}