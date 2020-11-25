package de.codecentric.vertx.koin.core.module

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import de.codecentric.vertx.koin.core.ModuleWithOrder
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
import de.codecentric.vertx.koin.core.toModuleWithOrder
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import java.io.File

class VertxKoinModule(vertx: Vertx? = null) : KoinModule {
    private val vertxKoinOrderedModule: ModuleWithOrder = module {
        single(VERTX_INSTANCE.qualifier) {
            setupVertxProperties()

            vertx ?: Vertx.vertx()
        }
    }.toModuleWithOrder(0, "vertxKoinOrderedModule")

    override val koinOrderedModules: LinkedHashSet<ModuleWithOrder> = linkedSetOf(vertxKoinOrderedModule)

    private fun setupVertxProperties() {
        System.setProperty("vertx.cwd", File(".").absolutePath.replace(".", ""))

        DatabindCodec.mapper().apply {
            registerKotlinModule()

            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
}

enum class VertxKoinQualifiers(val qualifier: StringQualifier) {
    VERTX_INSTANCE("VERTX_INSTANCE".qualifier()),
}
