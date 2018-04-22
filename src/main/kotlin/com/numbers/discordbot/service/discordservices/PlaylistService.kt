package com.numbers.discordbot.service.discordservices

import com.numbers.discordbot.module.music.Track
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import retrofit2.Call
import retrofit2.http.*
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser
import javax.inject.Inject

data class Playlist(val id: Long = 0, val user: String, val guild: String, val name: String, val songs: List<Song>)

data class Song(val id: Long = 0, val name: String, val url: String)

val Track.asSong: Song get() = Song(name = this.identifier, url = this.url)

interface PlaylistService {
    suspend fun getAllPlaylists(): List<Playlist> = getAllPlaylistsAsync().await()
    fun getAllPlaylistsAsync(): Deferred<List<Playlist>>

    suspend fun getPlaylistsForUser(user: IUser): List<Playlist> = getPlaylistsForUserAsync(user).await()
    fun getPlaylistsForUserAsync(user: IUser): Deferred<List<Playlist>>

    suspend fun getPlaylistsForGuild(guild: IGuild): List<Playlist> = getPlaylistsForGuildAsync(guild).await()
    fun getPlaylistsForGuildAsync(guild: IGuild): Deferred<List<Playlist>>

    suspend fun getPlaylistsByName(name: String): List<Playlist> = getPlaylistsByNameAsync(name).await()
    fun getPlaylistsByNameAsync(name: String): Deferred<List<Playlist>>

    suspend fun addNewPlaylist(playlist: Playlist) = addNewPlaylistAsync(playlist).await()
    fun addNewPlaylistAsync(playlist: Playlist): Deferred<Unit>

    suspend fun deletePlaylist(playlist: Playlist) = deletePlaylistsAsync(playlist).await()
    fun deletePlaylistsAsync(playlist: Playlist): Deferred<Unit>

    suspend fun updatePlaylist(playlist: Playlist) = updatePlaylistAsync(playlist).await()
    fun updatePlaylistAsync(playlist: Playlist): Deferred<Unit>

    suspend fun addSongToPlaylist(playlist: Playlist, song: Song) = addSongToPlaylistAsync(playlist, song).await()
    fun addSongToPlaylistAsync(playlist: Playlist, song: Song): Deferred<Unit>
}

class InternalPlaylistService @Inject constructor(private val webService: PlaylistWebService) : PlaylistService {

    override fun getAllPlaylistsAsync(): Deferred<List<Playlist>> = async {
        webService.getAllPlaylists().execute().body().orEmpty()
    }

    override fun getPlaylistsForUserAsync(user: IUser): Deferred<List<Playlist>> = async {
        webService.getPlaylistsForUser(user.stringID).execute().body().orEmpty()
    }

    override fun getPlaylistsForGuildAsync(guild: IGuild): Deferred<List<Playlist>> = async {
        webService.getPlaylistsForGuild(guild.stringID).execute().body().orEmpty()
    }

    override fun getPlaylistsByNameAsync(name: String): Deferred<List<Playlist>> = async {
        webService.getPlaylistsByName(name).execute().body().orEmpty()
    }

    override fun addNewPlaylistAsync(playlist: Playlist): Deferred<Unit> = async {
        webService.addNewPlaylist(playlist).execute()
        return@async
    }

    override fun deletePlaylistsAsync(playlist: Playlist): Deferred<Unit> = async {
        webService.deletePlaylist(playlist.id.toString()).execute()
        return@async
    }

    override fun updatePlaylistAsync(playlist: Playlist): Deferred<Unit> = async {
        webService.updatePlaylist(playlist.id.toString(), playlist).execute()
        return@async
    }

    override fun addSongToPlaylistAsync(playlist: Playlist, song: Song): Deferred<Unit> = async {
        webService.addSongToPlaylist(playlist.id.toString(), song).execute()
        return@async
    }

}

interface PlaylistWebService {

    @GET("/playlists")
    fun getAllPlaylists(): Call<List<Playlist>>

    @GET("/playlists/user/{user-id}")
    fun getPlaylistsForUser(@Path("user-id") userId: String): Call<List<Playlist>>

    @GET("/playlists/guid/{guild-id}")
    fun getPlaylistsForGuild(@Path("{guild-id}") guildId: String): Call<List<Playlist>>

    @GET("/playlists/name/{name}")
    fun getPlaylistsByName(@Path("name") name: String): Call<List<Playlist>>

    @POST("/playlists")
    fun addNewPlaylist(@Body playlist: Playlist): Call<Void>

    @POST("/playlists/{playlist-id}")
    fun addSongToPlaylist(@Path("playlist-id") playlistId: String, @Body song: Song): Call<Void>

    @DELETE("/playlists/songs/{song-id}")
    fun deleteSong(@Path("song-id") songId: String): Call<Void>

    @DELETE("/playlists/{playlist-id}")
    fun deletePlaylist(@Path("playlist-id") playlistId: String): Call<Void>

    @PATCH("/playlists/{playlist-id}")
    fun updatePlaylist(@Path("playlist-id") playlistId: String, @Body playlist: Playlist): Call<Void>

}

