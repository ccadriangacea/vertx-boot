package de.codecentric.vertx.camel

import com.jayway.awaitility.Awaitility.await
import de.codecentric.vertx.boot.launcher.DefaultVertxLauncher
import de.codecentric.vertx.camel.module.VertxCamelKoinModule
import de.codecentric.vertx.camel.module.VertxCamelKoinQualifiers.CAMEL_CONTEXT
import de.codecentric.vertx.koin.test.VertxLauncherIntegrationTest
import de.codecentric.vertx.koin.test.extension.KoinVertxExtension
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.Message
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicBoolean

class CamelInOutVertxKoinTest : VertxLauncherIntegrationTest() {
    companion object {
        private val launcher = DefaultVertxLauncher(emptyArray())
            .apply {
                orderedModules.addAll(VertxCamelKoinModule().koinOrderedModules)
            }

        @Suppress("unused")
        @JvmField
        @RegisterExtension
        val koinVertxExtension = KoinVertxExtension(launcher)
    }

    private val camel: DefaultCamelContext by inject(CAMEL_CONTEXT.qualifier)

    @AfterEach
    fun tearDown() {
        camel.stop()
        vertx.close()
    }

    @Test
    fun `should run`() {
        val headersReceived = AtomicBoolean()
        val responseReceived = AtomicBoolean()

        camel.start()

        camel.addRoutes(object : RouteBuilder() {
            @Throws(Exception::class)
            override fun configure() {
                from("direct:input")
                    .inOut("direct:code-generator").process().body { body: Any, headers: Map<String, Any> ->

                        assertThat(body).isEqualTo("OK!")
//                        assertThat(h).contains(Assertions.entry("foo", "bar"))

                        headersReceived.set(true)
                    }

                from("direct:code-generator").to("vertx:code-generator")
            }
        })

        vertx.eventBus().consumer("code-generator") { msg: Message<Any> ->
            assertThat(msg.body().toString()).isEqualTo("hello")
//            assertThat(msg.headers().names()).hasSize(2).contains("headerA", "headerB")

            msg.reply("OK!", DeliveryOptions().addHeader("foo", "bar"))
        }

        camel
            .createProducerTemplate()
            .asyncRequestBodyAndHeaders("direct:input", "hello", mapOf("headerA" to "A", "headerB" to "B"), String::class.java)
            .thenAccept { body: Any ->
                assertThat(body).isEqualTo("OK!")
                responseReceived.set(true)
            }

        await().atMost(10, SECONDS).untilAtomic(headersReceived, Is.`is`(true))
        await().atMost(10, SECONDS).untilAtomic(responseReceived, Is.`is`(true))
        assertThat(true).isTrue
    }
}