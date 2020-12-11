package de.codecentric.util.fnresult

import de.codecentric.util.fnresult.exception.FnResultFutureException

fun <T> FnResult<T>.getResult(): T {
    require(this is FnResult.FnSuccess<T>)

    return this.result
}

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


fun <T> FnResult.FnError<T>.throwable() = FnResultFutureException(this.errorMessage)

fun <T, R> FnResult.FnError<T>.copy(): FnResult.FnError<R> = FnResult.FnError(this.errorMessage, this.cause, this.statusCode)
