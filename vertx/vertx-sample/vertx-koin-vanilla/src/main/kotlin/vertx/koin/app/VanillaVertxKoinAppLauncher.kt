@file:OptIn(ExperimentalTime::class)

package vertx.koin.app

import de.codecentric.koin.core.KoinComponentWithOptIn
import de.codecentric.vertx.koin.core.module.VertxKoinModule
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.Vertx
import org.koin.core.component.get
import org.koin.core.context.startKoin
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

object VanillaVertxKoinAppLauncher : KoinComponentWithOptIn {
    @JvmStatic
    fun main(args: Array<String>) {
        val vertx: Vertx
        val time: Duration = measureTime {
            startKoin {
                modules(VertxKoinModule(null).koinOrderedModules.map { it.module })
            }

            vertx = get<Vertx>(VERTX_INSTANCE.qualifier).apply { deployVerticle(MainVerticle()) }
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
