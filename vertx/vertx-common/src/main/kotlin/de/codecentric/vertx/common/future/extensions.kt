@file:Suppress("DuplicatedCode", "EXPERIMENTAL_API_USAGE")

package de.codecentric.vertx.common.future

import de.codecentric.vertx.common.util.doNothing
import io.vertx.core.Future
import io.vertx.core.Promise
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.concurrent.timer
import kotlin.random.Random

private const val TIMER_NAME_ID: Int = 1_000
private const val TIMER_TIMEOUT_DEFAULT_MESSAGE = "Your future timed out after "
private const val FUTURE_MAP_DEFAULT_TIMEOUT_MS = 5_000L
private const val FUTURE_MAP_DEFAULT_CHECK_PERIOD_MS = 100L
private const val TIMER_DELAY_PERIOD_MS = 10L // TODO check if this is enough?

// TODO does this have to work with nullable returns?
fun <I, O> Future<I>.mapWithTimeout(
    timeout: Long = FUTURE_MAP_DEFAULT_TIMEOUT_MS,
    checkFuturePeriod: Long = FUTURE_MAP_DEFAULT_CHECK_PERIOD_MS,
    timeoutMessage: String = TIMER_TIMEOUT_DEFAULT_MESSAGE,
    mapper: (I) -> O
): Future<O> {
    val resultPromise = Promise.promise<O>()

    val deferredFutureToWatch = GlobalScope.async { this@mapWithTimeout.map(mapper) }

    val timerForFuture = timer(name = "timerForFuture_${Random.nextInt(TIMER_NAME_ID)}", daemon = false, initialDelay = 0, period = checkFuturePeriod) {
        when (deferredFutureToWatch.isCompleted) {
            true -> {
                this@timer.cancel()
                resultPromise.complete(deferredFutureToWatch.getCompleted().result())
            }
            false -> doNothing()
        }
    }

    timer(name = "timerForTimer_${Random.nextInt(TIMER_NAME_ID)}", daemon = false, initialDelay = timeout + TIMER_DELAY_PERIOD_MS, period = checkFuturePeriod) {
        timerForFuture.cancel()
        this@timer.cancel()

        when (deferredFutureToWatch.isCompleted) {
            true -> doNothing()
            false -> resultPromise.fail(timeoutMessage.plus(timeout))
        }
    }

    return resultPromise.future()
}

fun <O> Future<*>.mapEmptyWithTimeout(
    timeout: Long = FUTURE_MAP_DEFAULT_TIMEOUT_MS,
    checkFuturePeriod: Long = FUTURE_MAP_DEFAULT_CHECK_PERIOD_MS,
    timeoutMessage: String = TIMER_TIMEOUT_DEFAULT_MESSAGE
): Future<O> {
    val resultPromise = Promise.promise<O>()

    val deferredFutureToWatch = GlobalScope.async { this@mapEmptyWithTimeout.mapEmpty<O>() }

    val timerForFuture = timer(name = "timerForFuture_${Random.nextInt(TIMER_NAME_ID)}", daemon = false, initialDelay = 0, period = checkFuturePeriod) {
        when (deferredFutureToWatch.isCompleted) {
            true -> {
                this@timer.cancel()
                resultPromise.complete(deferredFutureToWatch.getCompleted().result())
            }
            false -> doNothing()
        }
    }

    timer(name = "timerForTimer_${Random.nextInt(TIMER_NAME_ID)}", daemon = false, initialDelay = timeout + TIMER_DELAY_PERIOD_MS, period = checkFuturePeriod) {
        timerForFuture.cancel()
        this@timer.cancel()

        when (deferredFutureToWatch.isCompleted) {
            true -> doNothing()
            false -> resultPromise.fail(timeoutMessage.plus(timeout))
        }
    }

    return resultPromise.future()
}

fun <I, O> Future<I>.composeWithTimeout(
    timeout: Long = FUTURE_MAP_DEFAULT_TIMEOUT_MS,
    checkFuturePeriod: Long = FUTURE_MAP_DEFAULT_CHECK_PERIOD_MS,
    timeoutMessage: String = TIMER_TIMEOUT_DEFAULT_MESSAGE,
    mapper: (I) -> Future<O>
): Future<O> {
    val resultPromise = Promise.promise<O>()

    val deferredFutureToWatch = GlobalScope.async { this@composeWithTimeout.compose<O>(mapper) }

    val timerForFuture = timer(name = "timerForFuture_${Random.nextInt(TIMER_NAME_ID)}", daemon = false, initialDelay = 0, period = checkFuturePeriod) {
        when (deferredFutureToWatch.isCompleted) {
            true -> {
                this@timer.cancel()
                resultPromise.complete(deferredFutureToWatch.getCompleted().result())
            }
            false -> doNothing()
        }
    }

    timer(name = "timerForTimer_${Random.nextInt(TIMER_NAME_ID)}", daemon = false, initialDelay = timeout + TIMER_DELAY_PERIOD_MS, period = checkFuturePeriod) {
        timerForFuture.cancel()
        this@timer.cancel()

        when (deferredFutureToWatch.isCompleted) {
            true -> doNothing()
            false -> resultPromise.fail(timeoutMessage.plus(timeout))
        }
    }

    return resultPromise.future()
}
