@file:Suppress("unused")

package de.codecentric.vertx.camel

import de.codecentric.kotlin.logger.loggerWithTab
import de.codecentric.util.fnresult.handleThrowable
import de.codecentric.util.fnresult.onFailureEmpty
import de.codecentric.vertx.boot.launcher.DefaultVertxLauncher
import de.codecentric.vertx.boot.verticle.KoinCoroutineVerticle
import de.codecentric.vertx.boot.verticle.logEndOfStart
import de.codecentric.vertx.camel.module.VertxCamelKoinModule
import de.codecentric.vertx.camel.module.VertxCamelKoinQualifiers.CAMEL_CONTEXT
import de.codecentric.vertx.koin.test.VertxLauncherIntegrationTest
import de.codecentric.vertx.koin.test.extension.KoinVertxExtension
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.Message
import io.vertx.junit5.VertxTestContext
import mu.KotlinLogging
import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.LoggingLevel.*
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.ExchangeHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.get
import org.koin.core.component.inject

private val kLogger = KotlinLogging.logger { }

private const val camelInputEndpoint = "direct:input"
private const val camelInOutEndpoint = "direct:in-out"
private const val vertxInOutAddress = "in.out"
private const val vertxCheckpointAddress = "checkpoint"
private const val initialMessage = "hello"
private const val vertxInOutMessage = "world"
private const val vertxCheckoutMessage = "done"

internal class VertxCamelMainVerticleTest : KoinCoroutineVerticle() {
    private val camel: DefaultCamelContext by inject(CAMEL_CONTEXT.qualifier)

    override suspend fun start() {
        super.start()

        val routeBuilder = object : RouteBuilder() {
            override fun configure() {
                from(camelInputEndpoint)
                    .log(WARN, "body: \${body} -> headers: \${headers}")
                    .to("vertx:$vertxInOutAddress")
                    .to(camelInOutEndpoint)

                from(camelInOutEndpoint)
                    .log(WARN, "body: \${body} -> headers: \${headers}")
                    .process(SimpleProcessor())
                    .log(WARN, "body: \${body} -> headers: \${headers}")
                    .to("vertx:$vertxCheckpointAddress")
            }
        }
        camel.addRoutes(routeBuilder)

        logEndOfStart()
    }
}

class SimpleProcessor : Processor {
    override fun process(exchange: Exchange) {
        exchange.print("simpleProcessor")

        assertThat(exchange.getIn().body.toString()).isEqualTo(vertxInOutMessage)
        assertThat(exchange.getIn().headers.keys).containsAll(listOf("headerA", "headerB"))
        assertThat(exchange.getIn().headers.values.map { it.toString() }).containsAll(listOf("A", "B"))

        exchange.message.body = 1
    }
}

class CamelToVertxProcessor(
    private val vertx: Vertx,
    private val to: String,
    private val timeout: Long = 10_000L,
    private val copyHeaders: Boolean = true,
    private val isPublish: Boolean = false
) : Processor {
    override fun process(exchange: Exchange) {
        exchange.print("CamelToVertx")

        val inMessage = exchange.getIn()

        val deliveryOptions = DeliveryOptions().apply {
            if (copyHeaders) inMessage.headers.forEach { (k, v) -> addHeader(k, v.toString()) }
            if (timeout > 0) sendTimeout = timeout
        }

        handleThrowable {
            if (isPublish) {
                vertx.eventBus().publish(to, inMessage.body, deliveryOptions)
            } else {
                if (ExchangeHelper.isOutCapable(exchange)) {
                    vertx.eventBus().request<Any>(to, inMessage.body, deliveryOptions) { reply ->
                        when (reply.succeeded()) {
                            false -> exchange.setException(reply.cause())
                            true -> exchange.message.apply {
                                body = reply.result().body()
                                reply.result().headers().forEach { headers[it.key] = it.value }
                            }
                        }
                    }
                } else {
                    vertx.eventBus().send(to, inMessage.body, deliveryOptions)
                }
            }
        }
            .onFailureEmpty {
                exchange.setException(it.cause)
            }
    }

//    override fun processAsync(exchange: Exchange): CompletableFuture<Exchange> {
//        val callback = AsyncCallbackToCompletableFutureAdapter(exchange)
//        process(exchange, callback)
//        return callback.future
//    }
//
//    override fun process(exchange: Exchange, callback: AsyncCallback): Boolean {
//        exchange.print("CamelToVertx")
//        val inMessage = exchange.getIn()
//
//        val deliveryOptions = DeliveryOptions().apply {
//            if (copyHeaders) inMessage.headers.forEach { (k, v) -> addHeader(k, v.toString()) }
//            if (timeout > 0) sendTimeout = timeout
//        }
//
//        handleThrowable {
//            if (isPublish) {
//                vertx.eventBus().publish(to, inMessage.body, deliveryOptions)
//            } else {
//                if (ExchangeHelper.isOutCapable(exchange)) {
//                    println("TEST doing request")
//                    vertx.eventBus().request<Any>(to, inMessage.body, deliveryOptions) { reply ->
//                        when (reply.succeeded()) {
//                            false -> exchange.setException(reply.cause())
//                            true -> exchange.message.apply {
//                                body = reply.result().body()
//                                reply.result().headers().forEach { headers[it.key] = it.value }
//                            }
//                        }
//                        // continue callback
//                        callback.done(false)
//                    }
//                    return@handleThrowable false
//                } else {
//                    println("TEST doing send")
//                    vertx.eventBus().send(to, inMessage.body, deliveryOptions)
//                }
//            }
//        }
//            .onFailureEmpty {
//                exchange.setException(it.cause)
//            }
//
//        callback.done(true)
//        return true
//    }
}

fun <T> Message<T>.print() {
    kLogger.loggerWithTab { " -> eventbus[${this.address()}] headers: ${this.headers().map { "${it.key} -> ${it.value}" }}" }
    kLogger.loggerWithTab { " -> eventbus[${this.address()}] body: ${this.body()}" }
}

fun Exchange.print(name: String) {
    kLogger.loggerWithTab { " -> exchange[$name] headers: ${this.getIn().headers}" }
    kLogger.loggerWithTab { " -> exchange[$name] body: ${this.getIn().body}" }
}

class VertxCamelLauncherIntegrationTest : VertxLauncherIntegrationTest() {
    companion object {
        private val launcher = DefaultVertxLauncher(emptyArray())
            .apply {
                orderedModules.addAll(VertxCamelKoinModule().koinOrderedModules)

                mainVerticleClass = "de.codecentric.vertx.camel.VertxCamelMainVerticleTest"
            }

        @JvmField
        @RegisterExtension
        val koinVertxExtension = KoinVertxExtension(launcher)
    }

    @Test
    fun `should get a camel context`() {
        val camel: DefaultCamelContext = get(CAMEL_CONTEXT.qualifier)

        assertThat(camel).isNotNull
        assertThat(camel.isStarted).isTrue
    }

    @Test
    fun `should deploy main verticle`() {
        assertThat(vertx.deploymentIDs()).hasSize(1)
    }

    @Test
    fun `should get a ping from camel`(testContext: VertxTestContext) {
        // GIVEN the 2 event bus consumers in routes
        val checkpoint = testContext.checkpoint(2)

        vertx.eventBus().consumer<Any>(vertxInOutAddress) { message ->
            message.print()

            assertThat(message.body()).isEqualTo(initialMessage)

            message.reply(vertxInOutMessage, DeliveryOptions().addHeader("foo", "bar"))
        }

        vertx.eventBus().consumer<Any>(vertxCheckpointAddress) { message ->
            message.print()

            checkpoint.flag()

            message.reply(vertxCheckoutMessage)
        }

        // WHEN a first producer send something to the initial route
        // THEN the message goes to the 2 vertx consumers and returns
        get<DefaultCamelContext>(CAMEL_CONTEXT.qualifier)
            .createProducerTemplate()
            .asyncRequestBodyAndHeaders(camelInputEndpoint, initialMessage, mapOf("headerA" to "A", "headerB" to "B"))
            .thenAccept { x ->
                assertThat(x).isEqualTo(vertxCheckoutMessage)
                checkpoint.flag()
            }
    }
}
