package de.codecentric.vertx.koin.core.module

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import de.codecentric.koin.core.KoinModule
import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.koin.core.qualifier
import de.codecentric.koin.core.toKoinModuleWithOrder
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import java.io.File
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

class VertxKoinModule(vertx: Vertx? = null) : KoinModule {
    private val vertxKoinOrderedModule = module {
        single(VERTX_INSTANCE.qualifier) {
            setupVertxProperties()

            vertx ?: Vertx.vertx()
        }
    }.toKoinModuleWithOrder(0, "vertxKoinOrderedModule")

    override val koinOrderedModules: LinkedHashSet<KoinModuleWithOrder> = linkedSetOf(vertxKoinOrderedModule)

    private fun setupVertxProperties() {
        System.setProperty("vertx.cwd", File(".").absolutePath.replace(".", ""))

        DatabindCodec.mapper()
            .apply {
                registerKotlinModule()

                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
    }
}

enum class VertxKoinQualifiers(val qualifier: StringQualifier) {
    VERTX_INSTANCE("VERTX_INSTANCE".qualifier()),
}
