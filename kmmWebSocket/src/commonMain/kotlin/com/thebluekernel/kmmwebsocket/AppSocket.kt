package com.thebluekernel.kmmwebsocket

/**
 * Created by Ahmed Ibrahim on 06,March,2022
 */
class AppSocket(url: String, headers: Map<String, String>) {

    private val ws = PlatformSocket(url, headers)

    // Holding socket errors
    var socketError: Throwable? = null
        private set

    var currentState: ConnectionState = ConnectionState.READY
        private set(value) {
            if (field != value) {
                field = value
                stateListener?.invoke(value)
            }
        }

    var stateListener: ((ConnectionState) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(currentState)
        }

    var messageListener: ((msg: String) -> Unit)? = null

    var closingListener: ((code: Int, message: String) -> Unit)? = null

    private val socketEvents: PlatformSocketEvents = object : PlatformSocketEvents {
        override fun onOpen() {
            currentState = ConnectionState.CONNECTED
        }

        override fun onFailure(t: Throwable) {
            if (currentState != ConnectionState.CLOSED_NORMAL) {
                socketError = t
                currentState = ConnectionState.CLOSED
                closingListener?.invoke(999, t.message ?: "Closed with failure")
            }
        }

        override fun onMessage(msg: String) {
            messageListener?.invoke(msg)
        }

        override fun onClosed(code: Int, reason: String) {
            currentState = ConnectionState.CLOSED_NORMAL
            closingListener?.invoke(code, reason)
        }

    }

    init {
        ws.init(socketEvents)
    }

    fun connect() {
        currentState = ConnectionState.CONNECTING
        ws.openSocket()
    }

    fun disconnect() {
        ws.closeSocket(1000, "The user has closed the connection.")
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
    READY,
    CONNECTING,
    CONNECTED,
    CLOSED,
    CLOSED_NORMAL
}