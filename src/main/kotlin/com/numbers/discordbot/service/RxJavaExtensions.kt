package com.numbers.discordbot.service

import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

interface RxWebSocket<T> {
    val value: Observable<T>

    fun reconnect()
}

class RxOkHttpWebSocket<T> constructor(private val client: OkHttpClient, request: Request, private val gson: Gson, private val clazz: Class<T>) : RxWebSocket<T>, WebSocketListener(), ObservableOnSubscribe<T> {

    private val subscribers: MutableList<ObservableEmitter<T>> = mutableListOf()
    private var webSocket = client.newWebSocket(request, this)

    override val value: Observable<T> = Observable.create(this)

    override fun subscribe(emitter: ObservableEmitter<T>) {
        subscribers.add(emitter)
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        val type = gson.fromJson(text, clazz)
        subscribers.forEach { it.onNext(type) }
    }

    override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
        reconnect()
        //subscribers.forEach { it.onComplete() }
    }

    override fun reconnect() {
        webSocket.close(4999, "reconnecting")
        webSocket = client.newWebSocket(webSocket.request(), this)
    }

    companion object {
        inline operator fun <reified T> invoke(client: OkHttpClient, request: Request, gson: Gson): RxWebSocket<T> = RxOkHttpWebSocket(client, request, gson, T::class.java)
    }

}