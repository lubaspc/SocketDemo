package com.lubaspc.connectios.websocket

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

class WSClient(uri: URI, private val handler: WSInterface) : WebSocketClient(uri) {
    override fun onOpen(handshakedata: ServerHandshake?) {
        handler.onSuccessConnection()
    }

    override fun send(text: String?) {
        handler.onMessage("Yo", text)
        super.send(text)
    }

    override fun onMessage(message: String?) {
        handler.onMessage(uri.host, message)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        handler.onClose(reason)
    }

    override fun onError(ex: Exception) = handler.onError(ex)
}