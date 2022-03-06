package com.thebluekernel.kmmwebsocket

/**
 * Created by Ahmed Ibrahim on 06,March,2022
 */
class AppSocket(url: String, headers: Map<String, String>) {

    private val ws = PlatformSocket(url, headers)

    // Holding socket errors
    var socketError: Throwable? = null
        private set

    var currentState: ConnectionState = ConnectionState.CLOSED
        private set(value) {
            if (field != value) {
                field = value
                stateListener?.invoke(value)
            }
        }

    var stateListener: ((ConnectionState) -> Unit)? = null
        set(value) {
            if (field != value) {
                field = value
                value?.invoke(currentState)
            }
        }

    var messageListener: ((msg: String) -> Unit)? = null

    var closingListener: ((code: Int, message: String) -> Unit)? = null

    private val socketEvents: PlatformSocketEvents = object : PlatformSocketEvents {
        override fun onOpen() {
            currentState = ConnectionState.CONNECTED
            println("Connection status is: CONNECTED")
        }

        override fun onFailure(t: Throwable) {
            socketError = t
            currentState = ConnectionState.CLOSED
            println("Connection status is: CLOSED with failure: ${t.message}")
        }

        override fun onMessage(msg: String) {
            messageListener?.invoke(msg)
            println("Received new message over socket.")
        }

        override fun onClosed(code: Int, reason: String) {
            currentState = ConnectionState.CLOSED
            closingListener?.invoke(code, reason)
            println("Connection status is: CLOSED with code: $code and reason: $reason")
        }

    }

    init {
        ws.init(socketEvents)
    }

    fun connect() {
        // close connection first if already active
        if (currentState != ConnectionState.CLOSED) disconnect()
        socketError = null
        currentState = ConnectionState.CONNECTING
        ws.openSocket()
    }

    fun disconnect() {
        if (currentState != ConnectionState.CLOSED) {
            ws.closeSocket(1000, "The user has closed the connection.")
            currentState = ConnectionState.CLOSED
        }
    }

    fun send(msg: String) {
        // connect if not connected
        if (currentState != ConnectionState.CONNECTED) connect()
        ws.sendMessage(msg)
    }
}

/**
 * Contains different connection states
 */
enum class ConnectionState {
    CONNECTING,
    CONNECTED,
    CLOSED
}