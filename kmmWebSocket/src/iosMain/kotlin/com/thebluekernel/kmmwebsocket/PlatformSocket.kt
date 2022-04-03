package com.thebluekernel.kmmwebsocket

import platform.Foundation.*
import platform.darwin.NSObject

/**
 * Created by Ahmed Ibrahim on 06,March,2022
 */
internal actual class PlatformSocket actual constructor(
    url: String,
    headers: Map<String, String>
) {
    private val socketEndpoint = NSURL.URLWithString(url)!!
    private val socketRequest = NSMutableURLRequest.requestWithURL(socketEndpoint)
    private val requestHeaders = headers
    private var webSocket: NSURLSessionWebSocketTask? = null
    private var events: PlatformSocketEvents? = null
    private var retryCount: Int = 3

    actual fun init(events: PlatformSocketEvents) {
        this.events = events
    }

    actual fun openSocket() {
        val urlSession = NSURLSession.sessionWithConfiguration(
            configuration = NSURLSessionConfiguration.defaultSessionConfiguration(),
            delegate = object : NSObject(), NSURLSessionWebSocketDelegateProtocol {
                override fun URLSession(
                    session: NSURLSession,
                    webSocketTask: NSURLSessionWebSocketTask,
                    didOpenWithProtocol: String?
                ) {
                    events?.onOpen()
                    retryCount = 3
                }

                override fun URLSession(
                    session: NSURLSession,
                    webSocketTask: NSURLSessionWebSocketTask,
                    didCloseWithCode: NSURLSessionWebSocketCloseCode,
                    reason: NSData?
                ) {
                    closeSocket(didCloseWithCode.toInt(), reason?.description ?: "")
                }
            },
            delegateQueue = NSOperationQueue.currentQueue()
        )

        requestHeaders.forEach { socketRequest.setValue(it.value, it.key) }
        webSocket = urlSession.webSocketTaskWithRequest(socketRequest)
        listenMessages(events)
        webSocket?.resume()
    }

    actual fun closeSocket(code: Int, reason: String) {
        webSocket?.cancelWithCloseCode(code.toLong(), null)
        webSocket = null
        events?.onClosed(code, reason)
    }

    actual fun sendMessage(msg: String) {
        val message = NSURLSessionWebSocketMessage(msg)
        webSocket?.sendMessage(message) { err ->
            err?.let { println("send $msg error: $it") }
        }
    }

    private fun listenMessages(events: PlatformSocketEvents?) {
        webSocket?.receiveMessageWithCompletionHandler { message, nsError ->
            when {
//                nsError != null -> {
//                    events?.onFailure(Throwable(nsError.description))
//                }
                message != null -> {
                    message.string?.let { events?.onMessage(it) }
                }
            }
            if (retryCount >= 0) {
                listenMessages(events)
                retryCount --
            }
        }
    }

}