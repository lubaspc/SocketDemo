package com.lubaspc.connectios.websocket

import android.util.Log
import androidx.lifecycle.MutableLiveData
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

class WSServer(port: Int, private val handler: WSInterface.Server) :
    WebSocketServer(InetSocketAddress(port)) {

    fun sendMessage(txt: String) {
        handler.onMessage("Yo", txt)
        broadcast(txt)
    }

    override fun onMessage(conn: WebSocket, message: String) {
        handler.onMessage(conn.remoteSocketAddress.hostString, message)
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        handler.newClient(conn?.remoteSocketAddress?.hostString)
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        handler.onClose(reason)
    }

    override fun onError(conn: WebSocket?, ex: java.lang.Exception) = handler.onError(ex)

    override fun onStart() {
        connectionLostTimeout = 0
        connectionLostTimeout = 100
        handler.onSuccessConnection()
    }
}