package de.codecentric.util.fnresult

import de.codecentric.util.fnresult.exception.FnResultFutureException

fun <T> FnResult<T>.getResult(): T {
    require(this is FnResult.FnSuccess<T>) { "The expected result was not a FnSuccess: ${(this as FnResult.FnError).errorMessage}" }

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

fun <I, T : Iterable<I>, R> FnResult<T>.mapList(mapFn: (I) -> R): FnResult<List<R>> =
    when (this) {
        is FnResult.FnSuccess -> handleThrowable { this.result.map { mapFn(it) } }
        is FnResult.FnAsyncSuccessCancelled -> handleThrowable { this.result.map { mapFn(it) } }
        is FnResult.FnError -> this.copyList()
    }

fun <I, T : Iterable<I>, R> FnResult<T>.flatMap(mapFn: (I) -> Iterable<R>): FnResult<List<R>> =
    when (this) {
        is FnResult.FnSuccess -> handleThrowable { this.result.flatMap { mapFn(it) } }
        is FnResult.FnAsyncSuccessCancelled -> handleThrowable { this.result.flatMap { mapFn(it) } }
        is FnResult.FnError -> this.copyList()
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

fun <T> FnResult<T>.onCompletion(fn: (T) -> Unit): FnResult<T> =
    when (this) {
        is FnResult.FnSuccess -> fn(this.result).run { this@onCompletion }
        is FnResult.FnAsyncSuccessCancelled -> this
        is FnResult.FnError -> this
    }

fun <T> FnResult.FnError<T>.throwable() = FnResultFutureException(this.errorMessage)

fun <T, R> FnResult.FnError<T>.copy(): FnResult.FnError<R> = FnResult.FnError(this.errorMessage, this.cause, this.statusCode)

fun <I, T : Iterable<I>, R> FnResult.FnError<T>.copyList(): FnResult.FnError<List<R>> = FnResult.FnError(this.errorMessage, this.cause, this.statusCode)
