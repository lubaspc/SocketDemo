package com.lubaspc.connectios.websocket

import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception

interface WSInterface {
    fun onMessage(origin: String,message: String?)
    fun onSuccessConnection()
    fun onError(ex: Exception)
    fun onClose(reason: String?)
    interface Server: WSInterface{
        fun newClient(conn: String?)
    }
}