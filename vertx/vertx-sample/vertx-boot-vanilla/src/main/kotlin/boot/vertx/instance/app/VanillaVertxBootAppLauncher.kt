@file:OptIn(ExperimentalTime::class)

package boot.vertx.instance.app

import de.codecentric.vertx.boot.VertxBootLauncher
import de.codecentric.kotlin.logger.loggerWithTab
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import mu.KotlinLogging
import java.util.logging.Level
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private val kLogger = KotlinLogging.logger { }

class VanillaVertxBootAppLauncher(args: Array<String>) : VertxBootLauncher(args) {
    override var mainVerticleClass: String? = "boot.vertx.instance.app.MainVerticle"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val app: VanillaVertxBootAppLauncher
            val time: Duration = measureTime {
                app = VanillaVertxBootAppLauncher(args)
                app.run()
            }

            kLogger.warn { "Started in $time" }

            app.stop()
        }
    }
}

@Suppress("unused")
internal class MainVerticle : AbstractVerticle() {
    override fun start(startPromise: Promise<Void>) {
        kLogger.loggerWithTab(10, Level.INFO) { " -> Main verticle start" }
        startPromise.complete()
    }

    override fun stop(stopPromise: Promise<Void>) {
        kLogger.loggerWithTab(10, Level.INFO) { " <- Main verticle stop" }
        stopPromise.complete()
    }
}
