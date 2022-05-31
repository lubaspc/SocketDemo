package com.lubaspc.connectios.websocket

import android.util.Log
import java.net.URI

class WebSocket private constructor(
    private var wsServer: WSServer?,
    private var wsClient: WSClient?
) {
    companion object {
        private const val port = 50123

        @JvmStatic
        fun connServer(handler: WSInterface.Server): WebSocket {
            val wsServer = WSServer(50123, handler)
            wsServer.start()
            return WebSocket(wsServer = wsServer, wsClient = null)
        }

        @JvmStatic
        fun connClient(apiGateway: String, handler: WSInterface): WebSocket {
            val wsClient = WSClient(URI("ws://${apiGateway}:$port"), handler)
            wsClient.connect()
            return WebSocket(wsClient = wsClient, wsServer = null)
        }
    }

    val typeConnection: TypeConnection
        get() = if (wsClient == null) TypeConnection.SERVER
        else TypeConnection.CLIENT


    fun sendMessage(txt: String) {
        Log.d("connectWifi", txt)
        try {
            wsServer?.sendMessage(txt)
            wsClient?.send(txt)
        } catch (e: Exception) {
            wsServer?.onError(null, e)
            wsClient?.onError(e)
        }
    }

    fun disconnect() {
        wsServer?.stop()
        wsClient?.close()
    }

    fun reconnect() {
        wsClient?.connect()
        wsServer?.start()
    }

    enum class TypeConnection {
        CLIENT, SERVER
    }
}