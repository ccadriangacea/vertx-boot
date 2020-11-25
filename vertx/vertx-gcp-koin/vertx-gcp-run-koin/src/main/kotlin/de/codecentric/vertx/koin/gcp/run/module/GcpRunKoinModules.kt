package de.codecentric.vertx.koin.gcp.run.module

import de.codecentric.vertx.common.fn.getResult
import de.codecentric.vertx.common.fn.handleThrowableAsync
import de.codecentric.vertx.common.fn.map
import de.codecentric.vertx.koin.core.ModuleWithOrder
import de.codecentric.vertx.koin.core.module.KoinModule
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_CONFIG_RETRIEVER
import de.codecentric.vertx.koin.core.toModuleWithOrder
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinQualifiers.VERTX_HTTPSERVER_PORT_DEFAULT_PORT
import de.codecentric.vertx.koin.web.module.VertxWebCommonModule.getDefaultPort
import io.vertx.config.ConfigRetriever
import io.vertx.kotlin.config.getConfigAwait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.dsl.module
import java.util.LinkedHashSet

class GcpRunKoinModules : KoinModule {
    private val gcpRunHttpServerOrderedKoinModule = module {
        single(VERTX_HTTPSERVER_PORT_DEFAULT_PORT.qualifier, override = true) {
            val configRetriever: ConfigRetriever = get(VERTX_CONFIG_RETRIEVER.qualifier)

            runBlocking {
                withContext(Dispatchers.Default) {
                    handleThrowableAsync { configRetriever.getConfigAwait() }
                        .map {
                            if (it.containsKey("PORT")) {
                                it.getString("PORT").toInt()
                            } else {
                                getDefaultPort(configRetriever).getResult()
                            }
                        }
                }
            }.getResult()
        }
    }.toModuleWithOrder(1, moduleName = "gcpRunHttpServerOrderedKoinModule")

    override val koinOrderedModules: LinkedHashSet<ModuleWithOrder> = linkedSetOf(gcpRunHttpServerOrderedKoinModule)
}
