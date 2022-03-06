package com.thebluekernel.kmmwebsocket

/**
 * Created by Ahmed Ibrahim on 06,March,2022
 */

internal actual class PlatformSocket actual constructor(
    url: String,
    headers: Map<String, String>
) {
    actual fun init(events: PlatformSocketEvents) {
    }

    actual fun openSocket() {
    }

    actual fun closeSocket(code: Int, reason: String) {
    }

    actual fun sendMessage(msg: String) {
    }


}