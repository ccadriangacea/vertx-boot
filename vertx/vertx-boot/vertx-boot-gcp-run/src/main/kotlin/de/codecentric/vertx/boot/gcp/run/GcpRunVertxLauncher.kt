package de.codecentric.vertx.boot.gcp.run

import de.codecentric.koin.core.toKoinModuleWithOrder
import de.codecentric.vertx.boot.launcher.DefaultVertxLauncher
import de.codecentric.vertx.boot.verticle.KoinCoroutineVerticle
import de.codecentric.vertx.boot.verticle.logEndOfStart
import de.codecentric.vertx.koin.gcp.core.module.GcpCoreKoinModule
import de.codecentric.vertx.koin.gcp.core.module.GcpCoreKoinQualifiers
import de.codecentric.vertx.koin.gcp.run.module.GcpRunKoinModules
import de.codecentric.vertx.koin.web.handler.setAsRouteHandler
import de.codecentric.vertx.koin.web.module.PingSingleRouteHandler
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinModules
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinQualifiers.VERTX_HTTPSERVER_ROUTER_DEFAULT_PORT
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinModules
import io.vertx.ext.web.Router
import org.koin.core.component.inject
import org.koin.dsl.module

open class GcpRunVertxLauncher(args: Array<String>) : DefaultVertxLauncher(args) {
    protected lateinit var gcpProjectId: String

    private val gcpCredentialsOrderedKoinModule = module {
        single(GcpCoreKoinQualifiers.GCP_PROJECT_NAME.qualifier, override = true) {
            gcpProjectId
        }
    }.toKoinModuleWithOrder(1, moduleName = "gcpCredentialsOrderedKoinModule")

    override fun run() {
        orderedModules.addAll(VertxHttpServerKoinModules().koinOrderedModules)
        orderedModules.addAll(VertxWebHandlersKoinModules().koinOrderedModules)

        orderedModules.addAll(GcpCoreKoinModule().koinOrderedModules)
        orderedModules.addAll(GcpRunKoinModules().koinOrderedModules)

        orderedModules.add(gcpCredentialsOrderedKoinModule)

        super.run()
    }

    override var mainVerticleClass: String? = "de.codecentric.vertx.boot.gcp.run.GcpRunMainVerticle"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            GcpRunVertxLauncher(args)
                .apply { gcpProjectId = "project-vertx-boot" }
                .run()
        }
    }
}

class GcpRunMainVerticle : KoinCoroutineVerticle() {
    private val router: Router by inject(VERTX_HTTPSERVER_ROUTER_DEFAULT_PORT.qualifier)

    override suspend fun start() {
        super.start()

        PingSingleRouteHandler("/ping").setAsRouteHandler(router)

        logEndOfStart()
    }
}
