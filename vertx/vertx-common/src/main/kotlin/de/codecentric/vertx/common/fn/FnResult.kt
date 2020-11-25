package de.codecentric.vertx.common.fn

import de.codecentric.vertx.common.util.doNothing

sealed class FnResult<T> {
    data class FnSuccess<T>(val result: T) : FnResult<T>()
    data class FnAsyncSuccessCancelled<T>(val result: T) : FnResult<T>()

    data class FnError<T>(val errorMessage: String, val cause: Throwable? = null, val statusCode: Int = DEFAULT_HTTP_STATUS_CODE) : FnResult<T>() {
        constructor(cause: Throwable) : this(cause.localizedMessage, cause)

        constructor(errorMessage: String, statusCode: Int = DEFAULT_HTTP_STATUS_CODE) : this(errorMessage, null, statusCode)
    }

    companion object {
        private const val DEFAULT_HTTP_STATUS_CODE: Int = 500

        fun fnErrorEmpty(): FnError<Unit> = FnError(FnResultFutureException("Empty error"))
    }
}

fun <T> FnResult<T>.getResult(): T {
    require(this is FnResult.FnSuccess<T>)

    return this.result
}

fun <T> FnResult<T>.getResultAsync(): T {
    require(this is FnResult.FnAsyncSuccessCancelled<T>)

    return this.result
}

fun <T> FnResult.FnError<T>.throwable() = FnResultFutureException(this.errorMessage)

class FnResultFutureException(override val message: String) : Throwable(message)

fun <T, R> FnResult.FnError<T>.copy(): FnResult.FnError<R> = FnResult.FnError(this.errorMessage, this.cause, this.statusCode)

fun <T> FnResult<T>.peek(mapFn: (T) -> Unit): FnResult<T> =
    when (this) {
        is FnResult.FnSuccess -> handleThrowable { mapFn(this.result) }.map { this.result }
        is FnResult.FnAsyncSuccessCancelled -> handleThrowable { mapFn(this.result) }.map { this.result }
        is FnResult.FnError -> this.copy<T, T>()
    }

fun <T, R> FnResult<T>.map(mapFn: (T) -> R): FnResult<R> =
    when (this) {
        is FnResult.FnSuccess -> handleThrowable { mapFn(this.result) }
        is FnResult.FnAsyncSuccessCancelled -> handleThrowable { mapFn(this.result) }
        is FnResult.FnError -> this.copy<T, R>()
    }

fun <T, R> FnResult<T>.compose(composeFn: (T) -> FnResult<R>): FnResult<R> =
    when (this) {
        is FnResult.FnSuccess -> handleThrowableOfFnResult { composeFn(this.result) }
        is FnResult.FnAsyncSuccessCancelled -> handleThrowableOfFnResult { composeFn(this.result) }
        is FnResult.FnError -> this.copy<T, R>()
    }

fun <T> FnResult<T>.onFailure(errorFn: (FnResult.FnError<T>) -> FnResult<T>): FnResult<T> =
    when (this) {
        is FnResult.FnSuccess -> this
        is FnResult.FnAsyncSuccessCancelled -> this
        is FnResult.FnError -> errorFn(this)
    }

fun <T> FnResult<T>.onFailureEmpty(errorFn: (FnResult.FnError<T>) -> Unit): Unit =
    when (this) {
        is FnResult.FnSuccess -> doNothing()
        is FnResult.FnAsyncSuccessCancelled -> doNothing()
        is FnResult.FnError -> errorFn(this)
    }

fun <T> handleThrowable(function: () -> T): FnResult<T> =
    try {
        FnResult.FnSuccess(function())
    } catch (th: Throwable) {
        FnResult.FnError(th)
    }

fun <T> handleThrowableOfFnResult(function: () -> FnResult<T>): FnResult<T> =
    try {
        function()
    } catch (th: Throwable) {
        FnResult.FnError(th)
    }

// Async
suspend fun <T, R> FnResult<T>.mapAsync(mapFn: suspend (T) -> R): FnResult<R> =
    when (this) {
        is FnResult.FnError -> this.copy<T, R>()
        is FnResult.FnSuccess -> handleThrowableAsync { mapFn(this.result) }
        is FnResult.FnAsyncSuccessCancelled -> handleThrowableAsync { mapFn(this.result) }
    }

suspend fun <T, R> FnResult<T>.composeAsync(composeFn: suspend (T) -> FnResult<R>): FnResult<R> =
    when (this) {
        is FnResult.FnError -> this.copy<T, R>()
        is FnResult.FnSuccess -> handleThrowableOfFnResultAsync { composeFn(this.result) }
        is FnResult.FnAsyncSuccessCancelled -> handleThrowableOfFnResultAsync { composeFn(this.result) }
    }

suspend fun <T> handleThrowableAsync(function: suspend () -> T): FnResult<T> =
    try {
        FnResult.FnSuccess(function())
    } catch (th: Throwable) {
        System.err.println("Got an exception in handleThrowableAsync...")
        FnResult.FnError(errorMessage = "something went wrong", cause = th)
    }

suspend fun <T> handleThrowableOfFnResultAsync(function: suspend () -> FnResult<T>): FnResult<T> =
    try {
        function()
    } catch (th: Throwable) {
        System.err.println("Got an exception in handleThrowableOfFnResultAsync...")
        FnResult.FnError(th)
    }

suspend fun <T> FnResult<T>.onFailureAsync(errorFn: suspend (FnResult.FnError<T>) -> FnResult<T>): FnResult<T> =
    when (this) {
        is FnResult.FnSuccess -> this
        is FnResult.FnAsyncSuccessCancelled -> this
        is FnResult.FnError -> errorFn(this)
    }

suspend fun <T> FnResult<T>.onFailureEmptyAsync(errorFn: suspend (FnResult.FnError<T>) -> Unit): Unit =
    when (this) {
        is FnResult.FnSuccess -> doNothing()
        is FnResult.FnAsyncSuccessCancelled -> doNothing()
        is FnResult.FnError -> errorFn(this)
    }
