package de.codecentric.vertx.koin.test

import de.codecentric.koin.core.KoinComponentWithOptIn
import de.codecentric.vertx.common.future.mapWithTimeout
import de.codecentric.vertx.koin.core.logger.loggerWithTab
import io.vertx.core.CompositeFuture
import io.vertx.core.Vertx
import io.vertx.core.VertxException
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.logger.Logger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

open class VertxLauncherIntegrationTest : KoinComponentWithOptIn {
    lateinit var vertx: Vertx

    companion object {
        @JvmField
        @RegisterExtension
        val vertxExtension = VertxExtension()
    }

    private lateinit var logger: Logger

    @BeforeEach
    fun beforeEach() {
        logger = getKoin().logger
        logger.loggerWithTab(4) { " -> beforeEach in VertxLauncherTest" }

        // waitForMainVerticle()

        logger.loggerWithTab(4) { " <- beforeEach in VertxLauncherTest" }
    }

    private fun waitForMainVerticle() {
        val countDownLatch = CountDownLatch(1)

        timer("mainVerticleDeploymentTimer", false, initialDelay = 1L, period = 50L) {
            logger.debug("Waiting for MainVerticle...")
            if (vertx.deploymentIDs().size > 0) {
                this.cancel()
                countDownLatch.countDown()
            }
        }

        try {
            if (!countDownLatch.await(500, TimeUnit.MILLISECONDS)) {
                logger.error("Timed out in starting main verticle")
                throw VertxException("MainVerticle not deployed")
            }
            logger.debug("done waiting for MainVerticle...")
        } catch (e: InterruptedException) {
            logger.error("Thread interrupted in startup")
            Thread.currentThread().interrupt()
            throw VertxException("MainVerticle not deployed")
        }
    }

    @AfterEach
    fun afterEach(verticleVertxTestContext: VertxTestContext) {
        val startingDebugTab = 4
        logger.loggerWithTab(startingDebugTab) { " -> afterEach in VertxLauncherTest" }

        val verticleIds = vertx.sharedData().getLocalMap<String, String>("undeploy").keys
        logger.debug("Undeploying ${verticleIds.size} verticles: $verticleIds")

        CompositeFuture.all(verticleIds.map {
            vertx.sharedData().getLocalMap<String, String>("undeploy").remove(it)
            vertx.undeploy(it)
        })
            .mapWithTimeout { logger.debug("Done undeploying...") }
            .onFailure { verticleVertxTestContext.failNow(it).also { logger.loggerWithTab(startingDebugTab) { " <- afterEach in VertxLauncherTest" } } }
            .onSuccess { verticleVertxTestContext.completeNow().also { logger.loggerWithTab(startingDebugTab) { " <- afterEach in VertxLauncherTest" } } }
    }
}
