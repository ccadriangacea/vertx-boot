@file:OptIn(ExperimentalTime::class)

package vertx.app

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.Vertx
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

object VertxVanilla {
    @JvmStatic
    fun main(args: Array<String>) {
        val vertx: Vertx
        val time: Duration = measureTime {
            vertx = Vertx.vertx().apply { deployVerticle(MainVerticle()) }
        }

        println("Started in $time")
        vertx.close()
    }
}

internal class MainVerticle : AbstractVerticle() {
    override fun start(startPromise: Promise<Void>) {
        println("Main verticle start")
        startPromise.complete()
    }

    override fun stop(stopPromise: Promise<Void>) {
        println("Main verticle stop")
        stopPromise.complete()
    }
}