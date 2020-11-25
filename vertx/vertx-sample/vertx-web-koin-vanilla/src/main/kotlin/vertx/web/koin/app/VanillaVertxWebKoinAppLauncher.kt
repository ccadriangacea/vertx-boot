@file:OptIn(ExperimentalTime::class)

package vertx.web.koin.app

import de.codecentric.vertx.koin.core.module.VertxConfigKoinModule
import de.codecentric.vertx.koin.core.module.VertxKoinModule
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers
import de.codecentric.vertx.koin.core.verticle.KoinComponentWithOptIn
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinModules
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinQualifiers.VERTX_HTTPSERVER_DEFAULT_PORT
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinModules
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

object VanillaVertxWebKoinAppLauncher : KoinComponentWithOptIn {
    @JvmStatic
    fun main(args: Array<String>) {
        val vertx: Vertx
        val time: Duration = measureTime {
            startKoin {
                modules(VertxKoinModule(null).koinOrderedModules.map { it.module })
                modules(VertxConfigKoinModule().koinOrderedModules.map { it.module })
                modules(VertxHttpServerKoinModules().koinOrderedModules.map { it.module })
                modules(VertxWebHandlersKoinModules().koinOrderedModules.map { it.module })
            }

            vertx = get<Vertx>(VertxKoinQualifiers.VERTX_INSTANCE.qualifier).apply { deployVerticle(MainVerticle()) }
        }

        println("Started in $time")
        vertx.close()
    }
}

@Suppress("unused")
class HttpServerMainVerticle : AbstractVerticle(), KoinComponentWithOptIn {
    private val httpServer: HttpServer by inject(VERTX_HTTPSERVER_DEFAULT_PORT.qualifier)
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
