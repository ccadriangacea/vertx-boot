package de.codecentric.vertx.boot.web.handler

import de.codecentric.util.fnresult.handleThrowable
import de.codecentric.vertx.boot.verticle.KoinCoroutineVerticle
import de.codecentric.vertx.boot.verticle.logEndOfStart
import de.codecentric.vertx.boot.web.HttpServerVertxLauncher
import de.codecentric.vertx.koin.test.VertxLauncherIntegrationTest
import de.codecentric.vertx.koin.test.extension.KoinVertxExtension
import de.codecentric.vertx.koin.test.extension.softly
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResult.Companion.DEFAULT_RESPONSE_BODY_KEY
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResult.HttpNotFound
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResult.HttpOk
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResultType.APPLICATION_JSON
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResultType.TEXT_PLAIN
import de.codecentric.vertx.koin.web.fn.mapToRoutingContextFnResult
import de.codecentric.vertx.koin.web.handler.SingleRouteHandler
import de.codecentric.vertx.koin.web.handler.setAsRouteHandler
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinQualifiers.VERTX_HTTPSERVER_DEFAULT_PORT
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinQualifiers.VERTX_HTTPSERVER_ROUTER_DEFAULT_PORT
import de.codecentric.vertx.koin.webclient.module.VertxWebclientKoinModule
import de.codecentric.vertx.koin.webclient.module.VertxWebclientKoinQualifiers.VERTX_WEB_CLIENT
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.junit5.Checkpoint
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KLogger
import mu.KotlinLogging
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.get
import org.koin.core.component.inject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.system.measureTimeMillis

private val logger: KLogger = KotlinLogging.logger {}

private const val echoStringResponse = "Ok."
private val echoJsonObjectResponse = JsonObject(mapOf("key" to "value"))

@Suppress("unused")
internal class SingleRouteHandlerTestVerticle : KoinCoroutineVerticle() {
    private val httpServer: HttpServer = get(VERTX_HTTPSERVER_DEFAULT_PORT.qualifier)
    private val router: Router by inject(VERTX_HTTPSERVER_ROUTER_DEFAULT_PORT.qualifier)
    private val webClient: WebClient by inject(VERTX_WEB_CLIENT.qualifier)

    override suspend fun start() {
        super.start()

        EchoSingleRouteHandler("/echo").setAsRouteHandler(router)
        AsyncSingleRouteHandler("/async").setAsRouteHandler(router)
        AsyncErrSingleRouteHandler("/asyncErr").setAsRouteHandler(router)
        AsyncExSingleRouteHandler("/asyncEx").setAsRouteHandler(router)
        AsyncExSingleRouteHandler("/asyncHandleEx").setAsRouteHandler(router)

        logEndOfStart()
    }

    inner class EchoSingleRouteHandler(override val basePath: String) :
        SingleRouteHandler(
            basePath = basePath,
            getHandlerAsync = { HttpOk(echoStringResponse, TEXT_PLAIN) },
            postHandlerAsync = { HttpOk(json { obj(DEFAULT_RESPONSE_BODY_KEY to it.bodyAsJson) }, APPLICATION_JSON) }
        )

    inner class AsyncSingleRouteHandler(override val basePath: String) :
        SingleRouteHandler(basePath, getHandlerAsync = { take5() })

    inner class AsyncErrSingleRouteHandler(override val basePath: String) :
        SingleRouteHandler(basePath, getHandlerAsync = { HttpNotFound<Void>(Exception("error in take5")) })

    inner class AsyncExSingleRouteHandler(override val basePath: String) :
        SingleRouteHandler(basePath, getHandlerAsync = { throw NullPointerException("exception in take5") })

    inner class AsyncHandleExSingleRouteHandler(override val basePath: String) :
        SingleRouteHandler(
            basePath,
            getHandlerAsync = { handleThrowable { throw NullPointerException("handle exception in take5") }.mapToRoutingContextFnResult() }
        )

    private val counter = AtomicInteger(0)

    private suspend fun take5(): HttpOk<Int> {
        val reply = counter.incrementAndGet()
        val delayTime = if (reply % 2 == 0) Random.nextLong(20) else Random.nextLong(5)
        // val delayTime = 0L // remove delay for quicker replies
        if (delayTime > 0) logger.debug { "delay[$reply]=$delayTime" }
        delay(TimeUnit.MILLISECONDS.toMillis(delayTime))
        return HttpOk(reply, TEXT_PLAIN)
    }
}

internal class SingleRouteHandlerIntegrationTest : VertxLauncherIntegrationTest() {
    companion object {
        private val launcher = HttpServerVertxLauncher(emptyArray())
            .apply {
                mainVerticleClass = "de.codecentric.vertx.boot.web.handler.SingleRouteHandlerTestVerticle"
                orderedModules.addAll(VertxWebclientKoinModule().koinOrderedModules)
            }

        @Suppress("unused")
        @JvmField
        @RegisterExtension
        val koinVertxExtension = KoinVertxExtension(launcher)
    }

    private val webClient: WebClient = getKoin().get(VERTX_WEB_CLIENT.qualifier)

    @Test
    fun `should start mainVerticle using launcher`() {
        assertThat(vertx.deploymentIDs().size).isEqualTo(1)
    }

    @Test
    fun `should reply to ping call`(dependenciesTestContext: VertxTestContext) {
        webClient.get(8081, "localhost", "/ping").send()
            .map { assertThat(it.bodyAsString() == "pong").isTrue }
            .mapEmpty<Void>()
            .onComplete(dependenciesTestContext.succeedingThenComplete())
    }

    @Test
    fun `should receive ok by calling GET of single route handler`(webCallTestContext: VertxTestContext) {
        webClient.get(8081, "localhost", "/echo").send()
            .map { httpResponse ->
                softly {
                    assertThat(httpResponse.statusCode()).isEqualTo(200)
                    assertThat(httpResponse.bodyAsString()).isEqualTo(echoStringResponse)
                }
            }
            .onComplete(webCallTestContext.succeedingThenComplete())
    }

    @Test
    fun `should receive same body by POST to single route handler`(webCallTestContext: VertxTestContext) {
        webClient.post(8081, "localhost", "/echo").sendJsonObject(echoJsonObjectResponse)
            .map { httpResponse ->
                softly {
                    assertThat(httpResponse.statusCode()).isEqualTo(200)
                    assertThat(httpResponse.bodyAsJsonObject()).isEqualTo(echoJsonObjectResponse)
                }
            }
            .onComplete(webCallTestContext.succeedingThenComplete())
    }

    @Test
    fun `should receive response from async endpoint`(webCallTestContext: VertxTestContext) {
        val counterSize = 5
        val checkpoint = webCallTestContext.checkpoint(counterSize)
        val replyCountDownLatch = CountDownLatch(counterSize)
        val callCountDownLatch = CountDownLatch(counterSize)

        runBlocking {
            var timeToMakeCalls: Long
            val timeToGetResponse = measureTimeMillis {
                timeToMakeCalls = measureTimeMillis {
                    repeat(IntRange(1, counterSize).count()) {
                        launch {
                            makeCallOnCoroutine(checkpoint, replyCountDownLatch)
                            callCountDownLatch.countDown()
                        } // use coroutines

                        // makeCallOnEventLoop(checkpoint, countDownLatch) // use futures

                        // delay(500) // Test async in call-reply
                    }

                    val doneCallCountDown = withContext(Dispatchers.Default) { callCountDownLatch.await(30, TimeUnit.SECONDS) }
                    if (!doneCallCountDown) throw TimeoutException("Not all calls are there!")
                }

                val doneCountDown = withContext(Dispatchers.Default) { replyCountDownLatch.await(30, TimeUnit.SECONDS) }
                if (!doneCountDown) throw TimeoutException("Not all responses are there!")
            }

            logger.info { "It took $timeToMakeCalls millis to make the calls and $timeToGetResponse millis to get the replies" }
        }
    }

    @Test
    fun `should receive 404 from asyncErr endpoint`(webCallTestContext: VertxTestContext) {
        webClient.get(8081, "localhost", "/asyncErr").send()
            .map { assertThat(it.statusCode()).isEqualTo(404) }
            .onComplete(webCallTestContext.succeedingThenComplete())
    }

    @Test
    fun `should receive 500 from asyncEx endpoint`(webCallTestContext: VertxTestContext) {
        webClient.get(8081, "localhost", "/asyncEx").send()
            .map { assertThat(it.statusCode()).isEqualTo(500) }
            .onComplete(webCallTestContext.succeedingThenComplete())
    }

    @Test
    fun `should receive 500 from asyncHandleEx endpoint`(webCallTestContext: VertxTestContext) {
        webClient.get(8081, "localhost", "/asyncHandleEx").send()
            .map { assertThat(it.statusCode()).isEqualTo(500) }
            .onComplete(webCallTestContext.succeedingThenComplete())
    }

    private suspend fun makeCallOnCoroutine(checkpoint: Checkpoint, replyCountDownLatch: CountDownLatch) {
        val httpResponse = webClient.get(8081, "localhost", "/async").send().await()

        printReply(httpResponse, checkpoint, replyCountDownLatch)
    }

    @Suppress("unused")
    private fun makeCallOnEventLoop(checkpoint: Checkpoint, replyCountDownLatch: CountDownLatch) {
        logger.info { "Calling..." }
        webClient.get(8081, "localhost", "/async").send().also { logger.info { "Called" } }
            .onSuccess { printReply(it, checkpoint, replyCountDownLatch) }
    }

    private fun printReply(httpResponse: HttpResponse<Buffer>, checkpoint: Checkpoint, replyCountDownLatch: CountDownLatch) {
        logger.debug { "Got ${httpResponse.statusCode()} - ${httpResponse.bodyAsString()}" }

        checkpoint.flag()
        replyCountDownLatch.countDown()
    }
}