package de.codecentric.vertx.camel.module

import de.codecentric.koin.core.KoinModule
import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.koin.core.qualifier
import de.codecentric.koin.core.toKoinModuleWithOrder
import de.codecentric.util.fnresult.handleThrowable
import de.codecentric.vertx.camel.module.VertxCamelKoinQualifiers.CAMEL_CONTEXT
import de.codecentric.vertx.camel.module.VertxCamelKoinQualifiers.CAMEL_SSL_CONTEXT_PARAMETERS
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
import io.vertx.core.Vertx
import org.apache.camel.component.vertx.VertxComponent
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.jsse.KeyManagersParameters
import org.apache.camel.support.jsse.KeyStoreParameters
import org.apache.camel.support.jsse.SSLContextParameters
import org.apache.camel.support.jsse.TrustManagersParameters
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import org.koin.dsl.onClose

class VertxCamelKoinModule : KoinModule {
    private val vertxCamelOrderedKoinModule = module {
        single(CAMEL_CONTEXT.qualifier, createdAtStart = true) {
            val vertx: Vertx = get(VERTX_INSTANCE.qualifier)

            DefaultCamelContext()
                .apply {
                    camelContextReference.getComponent("vertx", VertxComponent::class.java).apply { this.vertx = vertx }
                    start()
                }
        }
            .also { it onClose { camel -> handleThrowable { camel?.close() } } }

//        single(CAMEL_SSL_CONTEXT_PARAMETERS.qualifier) {
//            val keyStore = KeyStoreParameters()
//                .apply {
//                    password = "password"
//                    resource = "keystore.jks"
//                }
//            val keyManagersParameters = KeyManagersParameters()
//                .apply {
//                    this.keyPassword = "password"
//                    this.keyStore = keyStore
//                }
//
//            val trustStore = KeyStoreParameters()
//                .apply {
//                    password = "password"
//                    resource = "truststore.jks"
//                }
//            val trustManagersParameters = TrustManagersParameters()
//                .apply { this.keyStore = trustStore }
//
//            SSLContextParameters()
//                .apply {
//                    keyManagers = keyManagersParameters
//                    trustManagers = trustManagersParameters
//                }
//        }

    }.toKoinModuleWithOrder(moduleName = "vertxCamelOrderedKoinModule")

    override val koinOrderedModules: LinkedHashSet<KoinModuleWithOrder> = linkedSetOf(vertxCamelOrderedKoinModule)
}

enum class VertxCamelKoinQualifiers(val qualifier: StringQualifier) {
    CAMEL_CONTEXT("CAMEL_CONTEXT".qualifier()),
    CAMEL_SSL_CONTEXT_PARAMETERS("CAMEL_SSL_CONTEXT_PARAMETERS".qualifier())
    ;
}