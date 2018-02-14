package com.numbers.discordbot.service

import com.google.inject.Inject
import com.mongodb.async.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.numbers.discordbot.module.music.Track
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.async.findOne
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.experimental.suspendCoroutine

data class PlayList(
        var id: ObjectId? = null,
        var guildId: String? = null,
        var userId: String? = null,
        var name: String? = null,
        var songs: MutableList<String> = mutableListOf()
)

class PlaylistService @Inject constructor(db: MongoDatabase){

    private val playlists = db.getCollection("playlist", PlayList::class.java)

    suspend fun playlistsBy(user: IUser? = null, guild: IGuild? = null, name: String? = null, track: Track? = null) : Iterable<PlayList> = suspendCoroutine {
        cont ->

        val filters =  mutableListOf<Bson>()

        user?.let { filters.add(Filters.eq<String>(PlayList::userId.name, user.stringID)) }
        guild?.let { filters.add(Filters.eq<String>(PlayList::guildId.name, guild.stringID)) }
        name?.let { filters.add(Filters.eq<String>(PlayList::name.name, name)) }
        track?.let { filters.add(Filters.eq<String>(PlayList::songs.name, track.url) )}

        val list = ConcurrentLinkedQueue<PlayList>()

            playlists.find(Filters.and(filters)).forEach({ list += it }, { _ , error: Throwable? ->
                error?.let {
                    cont.resumeWithException(it)
                    return@forEach
                }

                cont.resume(list)
            })
    }

    suspend fun deletePlaylist(id: ObjectId) : Boolean = suspendCoroutine{ cont ->
        playlists.findOneAndDelete(Filters.eq("_id", id)) {
            playlist :PlayList? , error: Throwable? ->
            when {
                error != null -> cont.resumeWithException(error)
                playlist == null -> cont.resume(false)
                else -> cont.resume(true)
            }
        }
    }

    fun save(playList: PlayList){
        playlists.insertOne(playList) { _,_ -> }
    }

    fun save(tracks: Iterable<Track>, name: String,  forGuild: IGuild, forUser: IUser){
        val trackUrls = tracks.map { it.url }.toMutableList()

        val filter : Bson = Filters.and(
                Filters.eq<String>(PlayList::songs.name, name),
                Filters.eq<String>(PlayList::userId.name, forUser.stringID),
                Filters.eq<String>(PlayList::guildId.name, forGuild.stringID))

        playlists.findOne(filter){ playList: PlayList?, _ ->
            if(playList == null){
                playlists.insertOne(PlayList(guildId = forGuild.stringID, userId = forUser.stringID, name = name, songs = trackUrls)) { _,_ -> }
            }else{
                playlists.replaceOne(Filters.and(
                        Filters.eq<String>(PlayList::songs.name, name),
                        Filters.eq<String>(PlayList::userId.name, forUser.stringID),
                        Filters.eq<String>(PlayList::guildId.name, forGuild.stringID))
                        , playList.copy(songs = trackUrls.toMutableList())) { _,_ -> }
            }

        }


    }
}

