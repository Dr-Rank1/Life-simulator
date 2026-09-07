package com.maisha.game.data.remote

import java.io.IOException

/**
 * Failures from [RandomUserApi]. [RandomNameService] catches these and falls back to [com.maisha.game.data.NamePool].
 */
sealed class RandomUserException(
    message: String,
    cause: Throwable? = null
) : IOException(message, cause) {

    class HttpStatus(val code: Int) : RandomUserException("Random User API HTTP $code")

    class Timeout(cause: Throwable) : RandomUserException("Random User API timed out", cause)

    class Network(cause: Throwable) : RandomUserException("Random User API unreachable", cause)

    class InvalidResponse(
        detail: String,
        cause: Throwable? = null
    ) : RandomUserException("Random User API response invalid: $detail", cause)
}
