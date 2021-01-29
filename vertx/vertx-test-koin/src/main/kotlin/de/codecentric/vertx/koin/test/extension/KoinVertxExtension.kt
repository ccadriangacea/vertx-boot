package de.codecentric.vertx.koin.test.extension

import de.codecentric.kotlin.logger.defaultLogTabDelta
import de.codecentric.kotlin.logger.defaultStartLogTab
import de.codecentric.vertx.boot.launcher.VertxLauncher
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
import de.codecentric.vertx.koin.test.VertxLauncherIntegrationTest
import de.codecentric.kotlin.logger.loggerWithTab
import io.vertx.core.Vertx
import mu.KotlinLogging
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import java.util.logging.Level.FINER
import java.util.logging.Level.FINEST

private val kLogger = KotlinLogging.logger { }

class KoinVertxExtension(private val vertxLauncher: VertxLauncher) : BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor {
    override fun beforeAll(extensionContext: ExtensionContext) {
        kLogger.loggerWithTab(defaultLogTabDelta, FINER) { " -> beforeAll in KoinVertxExtension for launcher: $vertxLauncher with args: ${vertxLauncher.args.toList()}" }

        vertxLauncher.run()

        kLogger.loggerWithTab(defaultLogTabDelta, FINER) { " <- beforeAll in KoinVertxExtension done" }
    }

    override fun afterAll(extensionContext: ExtensionContext) {
        kLogger.loggerWithTab(defaultLogTabDelta, FINER) { " -> afterAll in KoinVertxExtension for launcher: $vertxLauncher with args: ${vertxLauncher.args.toList()}" }

        vertxLauncher.stop()

        kLogger.loggerWithTab(defaultLogTabDelta, FINER) { " <- afterAll in KoinVertxExtension done" }
    }

    override fun postProcessTestInstance(testInstance: Any, extensionContext: ExtensionContext) {
        kLogger.loggerWithTab(defaultLogTabDelta, FINEST) { " -> postProcessTestInstance in KoinVertxExtension" }

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

        kLogger.loggerWithTab(defaultLogTabDelta, FINER) { " <- postProcessTestInstance in KoinVertxExtension done" }
    }
}

fun softly(block: SoftAssertions.() -> Unit) = SoftAssertions().apply(block).assertAll()
