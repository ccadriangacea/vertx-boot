@file:Suppress("unused")

package de.codecentric.vertx.koin.firebase.auth.idtoken

import de.codecentric.vertx.boot.verticle.KoinCoroutineVerticle
import de.codecentric.vertx.boot.verticle.logEndOfStart
import de.codecentric.vertx.boot.web.HttpServerVertxLauncher
import de.codecentric.vertx.koin.firebase.auth.idtoken.module.FirebaseIdTokenAuthKoinModule
import de.codecentric.vertx.koin.firebase.auth.idtoken.module.FirebaseIdTokenKoinQualifiers.FIREBASE_ID_TOKEN_JWT_AUTH_HANDLER
import de.codecentric.vertx.koin.test.VertxLauncherIntegrationTest
import de.codecentric.vertx.koin.test.extension.KoinVertxExtension
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResult
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResultType
import de.codecentric.vertx.koin.web.handler.SingleRouteHandler
import de.codecentric.vertx.koin.web.handler.setAsRouteHandler
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinQualifiers.VERTX_HTTPSERVER_ROUTER_DEFAULT_PORT
import de.codecentric.vertx.koin.webclient.module.VertxWebclientKoinQualifiers.VERTX_WEB_CLIENT
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.junit5.VertxTestContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject

class FirebaseIdTokenAuthTestVerticle : KoinCoroutineVerticle() {
    private val router: Router by inject(VERTX_HTTPSERVER_ROUTER_DEFAULT_PORT.qualifier)
    private val jwtAuthHandler: JWTAuthHandler by inject(FIREBASE_ID_TOKEN_JWT_AUTH_HANDLER.qualifier)

    override suspend fun start() {
        super.start()

        router.route("/me").handler(jwtAuthHandler)

        MeSingleRouteHandler("/me").setAsRouteHandler(router)

        logEndOfStart()
    }

    inner class MeSingleRouteHandler(basePath: String) : SingleRouteHandler(
        basePath,
        getHandlerAsync = {
            RoutingContextFnResult.HttpOk("this is fine!", RoutingContextFnResultType.TEXT_PLAIN)
        }
    )
}

class FirebaseIdTokenAuthIntegrationTest : VertxLauncherIntegrationTest() {
    companion object {
        private val launcher = HttpServerVertxLauncher(emptyArray())
            .apply {
                mainVerticleClass = "de.codecentric.vertx.koin.firebase.auth.idtoken.FirebaseIdTokenAuthTestVerticle"

                orderedModules.addAll(FirebaseIdTokenAuthKoinModule().koinOrderedModules)
            }

        @JvmField
        @RegisterExtension
        val koinVertxExtension = KoinVertxExtension(launcher)

        private const val TOKEN: String = "change_locally"
    }

    private val webClient: WebClient = getKoin().get(VERTX_WEB_CLIENT.qualifier)

    @Test
    fun `should start mainVerticle using launcher`() {
        assertThat(vertx.deploymentIDs().size).isEqualTo(1)
    }

    @Test
    fun `should reply to me call when jwt is present`(dependenciesTestContext: VertxTestContext) {
        if (TOKEN == "change_locally") {
            dependenciesTestContext.completeNow()
            return
        }

        webClient.get(8081, "localhost", "/me")
            .bearerTokenAuthentication(TOKEN)
            .send()
            .map { assertThat(it.statusCode()).isEqualTo(200) }
            .mapEmpty<Void>()
            .onComplete(dependenciesTestContext.succeedingThenComplete())
    }

    @Test
    fun `should reply with 401 to me call when no jwt is provided`(dependenciesTestContext: VertxTestContext) {
        webClient.get(8081, "localhost", "/me").send()
            .map { assertThat(it.statusCode()).isEqualTo(401) }
            .mapEmpty<Void>()
            .onComplete(dependenciesTestContext.succeedingThenComplete())
    }
}