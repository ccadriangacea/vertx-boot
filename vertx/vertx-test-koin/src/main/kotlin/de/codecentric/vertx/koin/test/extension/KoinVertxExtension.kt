package de.codecentric.vertx.koin.test.extension

import de.codecentric.vertx.boot.launcher.VertxLauncher
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
import de.codecentric.vertx.koin.test.VertxLauncherIntegrationTest
import de.codecentric.vertx.logger.loggerWithTab
import io.vertx.core.Vertx
import mu.KotlinLogging
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import java.util.logging.Level.FINER

private val kLogger = KotlinLogging.logger { }

class KoinVertxExtension(private val vertxLauncher: VertxLauncher) : BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor {
    override fun beforeAll(extensionContext: ExtensionContext) {
        kLogger.loggerWithTab(10, FINER) { " -> beforeAll in KoinVertxExtension for launcher: $vertxLauncher with args: ${vertxLauncher.args.toList()}" }

        vertxLauncher.run()

        kLogger.loggerWithTab(10, FINER) { " <- beforeAll in KoinVertxExtension done" }
    }

    override fun afterAll(extensionContext: ExtensionContext) {
        kLogger.loggerWithTab(10, FINER) { " -> afterAll in KoinVertxExtension for launcher: $vertxLauncher with args: ${vertxLauncher.args.toList()}" }

        vertxLauncher.stop()

        kLogger.loggerWithTab(10, FINER) { " <- afterAll in KoinVertxExtension done" }
    }

    override fun postProcessTestInstance(testInstance: Any, extensionContext: ExtensionContext) {
        kLogger.loggerWithTab(10, FINER) { " -> postProcessTestInstance in KoinVertxExtension" }

        when (testInstance) {
            is VertxLauncherIntegrationTest -> testInstance.getKoin()
                .apply {
                    get<Vertx>(VERTX_INSTANCE.qualifier).also { vertx ->
                        extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).put("vertx", vertx)

                        testInstance.vertx = vertx
                    }
                }
            else -> TODO("Handling from other Test classes is not supported!")
        }

        kLogger.loggerWithTab(10, FINER) { " <- postProcessTestInstance in KoinVertxExtension done" }
    }
}

fun softly(block: SoftAssertions.() -> Unit) = SoftAssertions().apply(block).assertAll()
