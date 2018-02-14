package com.numbers.discordbot.module.music
import sx.blah.discord.handle.audio.IAudioProvider
import sx.blah.discord.handle.impl.obj.Embed
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.roundToInt




interface MusicPlayer : IAudioProvider {

    val eventListeners : MutableList<MusicEventListener>
    val currentTrack : Track? get() { return scheduler.current }
    var volume : Int
    var isPaused: Boolean
    var scheduler : Scheduler

    fun skip(amount: Int = 1)
    fun skipAll()

    fun search(search: String, user: IUser, callback: SearchResultHandler)

    fun add(track: Track) {
        scheduler.tracks += track
    }

    fun addToFront(track: Track){
        add(0, track)
    }

    fun add(index: Int, track: Track){
        scheduler.tracks.add(index, track)
    }

    fun remove(track: Track){
        scheduler.tracks -= track
    }

    fun seak(duration: Duration){
        if(currentTrack == null) return

        val point = duration.toMillis()
        if(point > currentTrack!!.duration.toMillis()) return

        currentTrack!!.seak(duration)
    }

}

interface MusicEventListener {

    fun onTrackStart(player: MusicPlayer,  track: Track)

    fun onTrackPause(player: MusicPlayer)
    fun onTrackResume(player: MusicPlayer)

    fun onTrackEnd(player: MusicPlayer, track: Track, reason: TrackEndReason)

    fun onTrackException(player: MusicPlayer, track: Track, exception: Exception)
    fun onTrackStuck(player: MusicPlayer, track: Track, durationStuck: Duration)

}

data class TrackEndReason(val mayStartNext: Boolean){

    companion object {
        val Finished = TrackEndReason(true)
        val LoadFailed = TrackEndReason(true)
        val Stopped = TrackEndReason(false)
        val Replaced = TrackEndReason(false)
        val Cleaned = TrackEndReason(false)
    }

}

interface SearchResultHandler{

    fun onFailed(search: String, exception: Exception)
    fun onFindOne(search: String, track: Track)
    fun onFindNone(search: String)
    fun onFindMultiple(search: String, tracks: Iterable<Track>)

}

interface Track {

    val requestedBy : IUser
    val duration : Duration
    val position : Duration
    val identifier : String
    val author : String
    val isStream : Boolean
    val url : String

    fun seak(duration: Duration)
}

fun Duration.format() : String {
    val seconds = (this.seconds % 60).toString().padStart(2, '0')
    val minutes = ((this.seconds / 60) % 60).toString().padStart(2, '0')
    val hours =  (((this.seconds / 60) / 60) % 24).toString().padStart(2, '0')

    if(hours == "00"){
        return "$minutes:$seconds"
    }

    return "$hours:$minutes:$seconds"
}

@JvmOverloads
fun Track.format(withPos : Boolean = false, withIndex: Int? = null) : String {

    val idFormatted = identifier.truncatePad(60)

    return if(!withPos){
        "```${if(withIndex != null) "${withIndex.toString().padStart(2,'0')}| " else ""}[${duration.format()}]\n$idFormatted``` [link]($url) [${requestedBy.mention(true)}]"
    }else{
        val dur1 = position.toMillis()
        val dur2 = duration.toMillis()

        //60 chars per line, minus the formatting of position and duration plus the two brackets for each format
        val freeSpace = 60 - ( (position.format().length + 2) + (duration.format().length + 2) )

        val halfPercentage = max(0, (dur1.toDouble() / dur2.toDouble() * freeSpace ).roundToInt() - 1)

        val array = CharArray(freeSpace) {'_'}
        array [halfPercentage] = '|'
        val control = String(array)

        "```[${position.format()}]$control[${duration.format()}]\n$idFormatted``` [link]($url) [${requestedBy.mention(true)}]"
    }
}

fun String.truncatePad(toLength: Long) : String{

    val japaneseUnicodeBlocks = object : HashSet<Character.UnicodeBlock>() {
        init {
            add(Character.UnicodeBlock.HIRAGANA)
            add(Character.UnicodeBlock.KATAKANA)
            add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
        }
    }



    val length = if( this.toCharArray().any { japaneseUnicodeBlocks.contains(Character.UnicodeBlock.of(it)) } ){
        toLength / 2
    }else{
        toLength
    }

    if(this.length <= length) return this.padEnd(toLength.toInt() - 1, '_')

    val string = this.substring(0, Math.max(0, length-3).toInt())
    if(toLength < 3) return string
    return "$string..."
}

fun String.truncate(toLength: Long) : String{
    if(this.length <= toLength) return this.padEnd(toLength.toInt(), '_')

    val string = this.substring(0, Math.max(0, toLength-3).toInt())
    if(toLength < 3) return string
    return "$string..."
}

interface Scheduler {

    val tracks: MutableList<Track>
    val remaining: List<Track> get() =  tracks
    val isInfinite: Boolean get() = false
    val current: Track?

    fun next() : Track?

    fun shuffle() {
        val current = this.current

        current?.let { tracks.removeAt(0) }
        tracks.shuffle()
        current?.let { tracks.add(0, it) }
    }

    fun skip()

}

class PlaylistScheduler(override val tracks : MutableList<Track> = mutableListOf()) : Scheduler{

    override val current: Track?
        get() = tracks.firstOrNull()

    override fun next(): Track? {
        if(!tracks.isEmpty()) tracks.removeAt(0)
        return current
    }

    override fun skip() {
        if(!tracks.isEmpty()){
            tracks.removeAt(0)
        }
    }



}

class RepeatlistScheduler(override val tracks : MutableList<Track> = mutableListOf()) : Scheduler{

    override val isInfinite = true

    override val remaining: List<Track>
        get() = tracks.subList(index, tracks.size)

    private var index = 0

    override val current: Track?
        get() = tracks[index]

    override fun next(): Track? {
        index++
        index %= tracks.size
        return current
    }

    override fun skip() {
        if(!tracks.isEmpty()){
            tracks.removeAt(index)
            index %= tracks.size
        }
    }

}

class RepeatSongScheduler(override val tracks : MutableList<Track> = mutableListOf()) : Scheduler{

    override val current: Track?
        get() = tracks.firstOrNull()

    override fun next(): Track? {
        return current
    }

    override fun skip() {
        if(!tracks.isEmpty()){
            tracks.removeAt(0)
        }
    }

}

fun List<Track>.toEmbeds(fromIndex : Int = 0): List<Embed.EmbedField>{
    fun toEmbed(queue: LinkedList<Track>, counter: AtomicInteger) : Embed.EmbedField{
        val builder = StringBuilder()

        while(!queue.isEmpty()){
            val formatted = queue.peekFirst().format(false, counter.getAndAdd(1))
            if(builder.length + formatted.length >= EmbedBuilder.FIELD_CONTENT_LIMIT-1){
                return Embed.EmbedField("_",builder.toString(), false)
            }else{
                queue.removeFirst()
                builder.append(formatted)
            }
        }
        return Embed.EmbedField("_",builder.toString(), false)
    }

    val list = mutableListOf<Embed.EmbedField>()

    val queue = LinkedList<Track>(this)

    var fields = 0
    val songCounter = AtomicInteger(fromIndex)

    while(fields < EmbedBuilder.FIELD_COUNT_LIMIT && !queue.isEmpty()){
        list += toEmbed(queue, songCounter)
        fields++
    }

    return list
}