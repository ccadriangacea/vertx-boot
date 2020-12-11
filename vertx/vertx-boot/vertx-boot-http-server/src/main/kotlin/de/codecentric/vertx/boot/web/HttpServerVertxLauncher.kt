package de.codecentric.vertx.boot.web

import de.codecentric.koin.core.KoinComponentWithOptIn
import de.codecentric.vertx.boot.launcher.DefaultVertxLauncher
import de.codecentric.vertx.koin.web.module.CustomPortVertxWebKoinModules
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinModules
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinQualifiers
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinModules
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpServer
import org.koin.core.component.inject

open class HttpServerVertxLauncher(args: Array<String>) : DefaultVertxLauncher(args) {
    override fun run() {
        orderedModules.addAll(VertxHttpServerKoinModules().koinOrderedModules)
        orderedModules.addAll(VertxWebHandlersKoinModules().koinOrderedModules)
        orderedModules.addAll(CustomPortVertxWebKoinModules().koinOrderedModules)

        super.run()
    }

    override var mainVerticleClass: String? = "de.codecentric.vertx.boot.web.HttpServerMainVerticle"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            HttpServerVertxLauncher(args).run()
        }
    }
}

@Suppress("unused")
internal class HttpServerMainVerticle : AbstractVerticle(), KoinComponentWithOptIn {
    private val httpServer: HttpServer by inject(VertxHttpServerKoinQualifiers.VERTX_HTTPSERVER_DEFAULT_PORT.qualifier)
}
