@file:Suppress("unused")

package de.codecentric.vertx.boot.web

import de.codecentric.vertx.koin.web.module.CustomPortVertxWebKoinQualifiers.VERTX_HTTPSERVER_CUSTOM_PORT
import de.codecentric.vertx.koin.web.module.CustomPortVertxWebKoinQualifiers.VERTX_HTTPSERVER_CUSTOM_PORT_SCOPE
import de.codecentric.vertx.koin.web.module.CustomPortVertxWebKoinQualifiers.VERTX_HTTPSERVER_OPTIONS_CUSTOM_PORT
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinQualifiers.VERTX_HTTPSERVER_DEFAULT_PORT
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinQualifiers.VERTX_HTTPSERVER_OPTIONS_DEFAULT_PORT
import de.codecentric.vertx.koin.test.VertxLauncherIntegrationTest
import de.codecentric.vertx.koin.test.extension.KoinVertxExtension
import de.codecentric.vertx.koin.test.extension.softly
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.parameter.parametersOf

class HttpServerVertxLauncherIntegrationTest : VertxLauncherIntegrationTest() {
    companion object {
        @JvmField
        @RegisterExtension
        val koinVertxExtension = KoinVertxExtension(HttpServerVertxLauncher(emptyArray()))
    }

    @Test
    fun `should get a HttpServer instance on default port 8081`() {
        val httpServerOptions: HttpServerOptions = getKoin().get(VERTX_HTTPSERVER_OPTIONS_DEFAULT_PORT.qualifier)
        val httpServer: HttpServer = getKoin().get(VERTX_HTTPSERVER_DEFAULT_PORT.qualifier)

        softly {
            assertThat(httpServerOptions.port).isEqualTo(8081)
            assertThat(httpServerOptions.compressionLevel).isEqualTo(1)

            assertThat(httpServer).isNotNull
            assertThat(httpServer.actualPort()).isEqualTo(8081)
        }
    }

    @Test
    fun `should get a custom HttpServer on port 8082`() {
        val customPortScope = getKoin().createScope("http-server-8082", VERTX_HTTPSERVER_CUSTOM_PORT_SCOPE.qualifier)

        val httpServerOptionsCustom: HttpServerOptions = customPortScope.get(VERTX_HTTPSERVER_OPTIONS_CUSTOM_PORT.qualifier) { parametersOf(8082) }
        val httpServerCustom: HttpServer = customPortScope.get(VERTX_HTTPSERVER_CUSTOM_PORT.qualifier) { parametersOf(8082) }

        softly {
            assertThat(httpServerOptionsCustom.port).isEqualTo(8082)
            assertThat(httpServerOptionsCustom.compressionLevel).isEqualTo(2)

            assertThat(httpServerCustom).isNotNull
            assertThat(httpServerCustom.actualPort()).isEqualTo(8082)
        }
    }
}
