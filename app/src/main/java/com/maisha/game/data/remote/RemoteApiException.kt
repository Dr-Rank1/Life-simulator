package com.maisha.game.data.remote

import java.io.IOException

/** Failures from public content APIs. Repositories catch these and keep using the local cache. */
sealed class RemoteApiException(
    message: String,
    cause: Throwable? = null
) : IOException(message, cause) {

    class HttpStatus(val code: Int) : RemoteApiException("Remote API HTTP $code")

    class Timeout(cause: Throwable) : RemoteApiException("Remote API timed out", cause)

    class Network(cause: Throwable) : RemoteApiException("Remote API unreachable", cause)

    class InvalidResponse(
        detail: String,
        cause: Throwable? = null
    ) : RemoteApiException("Remote API response invalid: $detail", cause)
}
