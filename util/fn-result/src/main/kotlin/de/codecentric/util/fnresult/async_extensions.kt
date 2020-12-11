package de.codecentric.util.fnresult

fun <T> FnResult<T>.getResultAsync(): T {
    require(this is FnResult.FnAsyncSuccessCancelled<T>)

    return this.result
}

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
