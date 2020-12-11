@file:Suppress("unused")

package de.codecentric.vertx.mongo

import de.codecentric.util.fnresult.handleThrowable
import de.codecentric.util.fnresult.handleThrowableAsync
import de.codecentric.util.fnresult.mapAsync
import de.codecentric.util.fnresult.onFailureEmpty
import de.codecentric.util.fnresult.onFailureEmptyAsync
import de.codecentric.vertx.boot.launcher.DefaultVertxLauncher
import de.codecentric.vertx.koin.test.VertxLauncherIntegrationTest
import de.codecentric.vertx.koin.test.extension.KoinVertxExtension
import de.codecentric.vertx.mongo.module.VertxMongodbKoinModule
import de.codecentric.vertx.mongo.module.VertxMongodbKoinQualifiers.VERTX_MONGODB_CLIENT_OPTIONS
import de.codecentric.vertx.mongo.module.VertxMongodbKoinQualifiers.VERTX_MONGODB_CLIENT_SHARED_POOL
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.get
import kotlin.random.Random

private val kLogger = KotlinLogging.logger { }

class VertxMongodbLauncherIntegrationTest : VertxLauncherIntegrationTest() {
    companion object {
        private val launcher = DefaultVertxLauncher(emptyArray())
            .apply { orderedModules.addAll(VertxMongodbKoinModule().koinOrderedModules) }

        @JvmField
        @RegisterExtension
        val koinVertxExtension = KoinVertxExtension(launcher)
    }

    @Test
    fun `should get a Mongo client option json`() {
        val options: JsonObject = get(VERTX_MONGODB_CLIENT_OPTIONS.qualifier)

        assertThat(options).isNotNull
        assertThat(options.getString("db_name")).isEqualTo("test")
    }

    @Test
    @Disabled("Use something like testcontainers to start a mongo or use the mongod lib")
    fun `should create a collection`(testContext: VertxTestContext) {
        runBlocking {
            val checkpoint = testContext.checkpoint(3)

            handleThrowable { get<MongoClient>(VERTX_MONGODB_CLIENT_SHARED_POOL.qualifier) }
                .mapAsync { client ->
                    assertThat(client).isNotNull

                    val collectionName = "test_${Random.nextInt(10_000)}"
                    handleThrowableAsync {
                        client.createCollection(collectionName).await()
                        kLogger.warn { "Done creating test" }
                        checkpoint.flag()
                    }
                        .mapAsync {
                            client.dropCollection(collectionName).await()
                            kLogger.warn { "Done deleting test" }
                            checkpoint.flag()
                        }
                        .mapAsync {
                            client.close().await()
                            kLogger.warn { "Closed connection" }
                            checkpoint.flag()
                        }
                        .onFailureEmptyAsync {
                            client.close().await()
                            kLogger.warn { "Closed connection after failure" }
                            testContext.failNow(it.cause)
                        }
                }
                .onFailureEmpty { testContext.failNow("This should not happen") }
        }
    }

    @Test
    @Disabled("Use something like testcontainers to start a mongo or use the mongod lib")
    fun `should close connection after a failure`(testContext: VertxTestContext) {
        runBlocking {
            handleThrowable { get<MongoClient>(VERTX_MONGODB_CLIENT_SHARED_POOL.qualifier) }
                .mapAsync { client ->
                    assertThat(client).isNotNull

                    handleThrowableAsync { client.createCollection("test").await() }
                        .mapAsync { testContext.failNow("This should not happen") }
                        .onFailureEmptyAsync {
                            client.close().await()
                            kLogger.warn { "Closed connection after failure" }
                            testContext.completeNow()
                        }
                }
                .onFailureEmpty { testContext.failNow("This should not happen") }
        }
    }
}
