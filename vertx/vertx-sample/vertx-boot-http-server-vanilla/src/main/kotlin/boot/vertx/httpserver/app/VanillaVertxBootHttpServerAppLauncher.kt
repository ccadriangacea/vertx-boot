@file:OptIn(ExperimentalTime::class)

package boot.vertx.httpserver.app

import de.codecentric.vertx.boot.verticle.KoinCoroutineVerticle
import de.codecentric.vertx.boot.verticle.logEndOfStart
import de.codecentric.vertx.boot.web.HttpServerVertxLauncher
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinQualifiers.VERTX_HTTPSERVER_DEFAULT_PORT
import io.vertx.core.http.HttpServer
import mu.KotlinLogging
import org.koin.core.component.inject
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private val kLogger = KotlinLogging.logger { }

class VanillaVertxBootHttpServerAppLauncher(args: Array<String>) : HttpServerVertxLauncher(args) {
    override var mainVerticleClass: String? = "boot.vertx.httpserver.app.MainVerticle"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val time: Duration = measureTime {
                VanillaVertxBootHttpServerAppLauncher(args)
                    .apply {
                        run()
                        stop()
                    }
            }

            kLogger.warn { "Started in $time" }
        }
    }
}

@Suppress("unused")
internal class MainVerticle : KoinCoroutineVerticle() {
    private val httpServer: HttpServer by inject(VERTX_HTTPSERVER_DEFAULT_PORT.qualifier)

    override suspend fun start() {
        super.start()

        logEndOfStart()
    }
}
