@file:Suppress("unused")

package de.codecentric.vertx.koin.test.launcher

import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.vertx.boot.launcher.AbstractVertxLauncher
import de.codecentric.vertx.boot.verticle.KoinCoroutineVerticle
import de.codecentric.vertx.boot.verticle.logEndOfStart
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.LinkedHashSet

open class TestVertxLauncher(
    override val mainVerticleClass: String = "de.codecentric.vertx.koin.test.launcher.TestExceptionMainVerticle",
    private val extraOrderedModules: LinkedHashSet<KoinModuleWithOrder> = linkedSetOf()
) : AbstractVertxLauncher(emptyArray()) {
    override fun run() {
        overrideModules.addAll(extraOrderedModules)

        super.run()
    }
}

const val DEFAULT_TEST_COUNTER_NAME = "TestCounter"

class TestVerticle : KoinCoroutineVerticle() {
    override suspend fun start() {
        super.start()

        vertx.sharedData().getCounter(DEFAULT_TEST_COUNTER_NAME).await().incrementAndGet()

        logEndOfStart()
    }
}

class TestExceptionMainVerticle : KoinCoroutineVerticle() {
    override suspend fun start() {
        super.start()

        coroutineScope {
            launch { vertx.deployVerticle("de.codecentric.vertx.koin.test.launcher.TestExceptionSubVerticle").await() }
        }

        logEndOfStart()
    }
}

class TestExceptionSubVerticle : KoinCoroutineVerticle() {
    override suspend fun start() {
        super.start()

        listOf(1, 2, 3)
            .map { throw IllegalArgumentException("Got exception in TestExceptionSubVerticle") }
    }
}
