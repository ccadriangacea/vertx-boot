package de.codecentric.vertx.koin.firebase.auth.idtoken.module

import de.codecentric.koin.core.KoinModule
import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.koin.core.qualifier
import de.codecentric.koin.core.toKoinModuleWithOrder
import de.codecentric.util.fnresult.getResult
import de.codecentric.util.fnresult.onFailure
import de.codecentric.util.fnresult.peek
import de.codecentric.vertx.koin.core.module.VertxConfigCommonModule.getActiveProfileProperty
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_CONFIG_RETRIEVER
import de.codecentric.vertx.koin.firebase.auth.idtoken.module.FirebaseIdTokenKoinQualifiers.FIREBASE_ID_TOKEN_AUTH_PROPERTIES
import de.codecentric.vertx.koin.firebase.auth.idtoken.module.FirebaseIdTokenKoinQualifiers.FIREBASE_ID_TOKEN_JWT_AUTH_HANDLER
import de.codecentric.vertx.koin.firebase.auth.idtoken.module.FirebaseIdTokenKoinQualifiers.FIREBASE_ID_TOKEN_JWT_AUTH_PROVIDER
import de.codecentric.vertx.koin.firebase.auth.idtoken.module.FirebaseIdTokenKoinQualifiers.FIREBASE_ID_TOKEN_KID_PUBLIC_KEYS
import de.codecentric.vertx.koin.firebase.auth.idtoken.properties.FirebaseIdTokenAuthApplicationProperties
import de.codecentric.vertx.koin.firebase.auth.idtoken.properties.FirebaseIdTokenAuthProperties
import de.codecentric.vertx.koin.firebase.auth.idtoken.provider.FirebaseIdTokenAuthenticationProvider
import de.codecentric.vertx.koin.webclient.module.VertxWebclientKoinModule
import de.codecentric.vertx.koin.webclient.module.VertxWebclientKoinQualifiers.VERTX_WEB_CLIENT
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.config.ConfigRetriever
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.ext.auth.keyStoreOptionsOf
import io.vertx.kotlin.ext.auth.pubSecKeyOptionsOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

class FirebaseIdTokenAuthKoinModule : KoinModule {
    private val firebaseIdTokenAuthOrderedKoinModule = module {
        single(FIREBASE_ID_TOKEN_AUTH_PROPERTIES.qualifier) {
            val configRetriever: ConfigRetriever = get(VERTX_CONFIG_RETRIEVER.qualifier)

            runBlocking {
                withContext(Dispatchers.Default) {
                    getActiveProfileProperty<FirebaseIdTokenAuthApplicationProperties, FirebaseIdTokenAuthProperties>(configRetriever)
                        .peek {
                            require(it.jwtOptions.algorithm.isNotBlank()) { "JwtOptions must include an algorithm" }
                            require(it.jwtOptions.issuer.isNotBlank()) { "JwtOptions must include an issuer" }
                            require(it.jwtOptions.audience.isNotEmpty()) { "JwtOptions must include an audience" }
                        }
                        .onFailure {
                            logger.error("Exception loading FirebaseIdTokenAuthProperties: ${it.cause} - ${it.errorMessage}")
                            throw RuntimeException("Missing JwtOptions. Without this validation of idTokens is not possible.")
                        }
                        .getResult()
                }
            }
        }

        single(FIREBASE_ID_TOKEN_JWT_AUTH_PROVIDER.qualifier) {
            val firebaseIdTokenAuthProperties: FirebaseIdTokenAuthProperties = get(FIREBASE_ID_TOKEN_AUTH_PROPERTIES.qualifier)
            val pubSecKeyOptions: List<PubSecKeyOptions> = get(FIREBASE_ID_TOKEN_KID_PUBLIC_KEYS.qualifier)

            val jwtAuthOptions: JWTAuthOptions = JWTAuthOptions().apply {
                jwtOptions = firebaseIdTokenAuthProperties.jwtOptions
                pubSecKeys = pubSecKeyOptions
                keyStore = keyStoreOptionsOf(password = "secret", path = "keystore.jceks", type = "jceks")
            }

            FirebaseIdTokenAuthenticationProvider(jwtAuthOptions)
        }

        single(FIREBASE_ID_TOKEN_JWT_AUTH_HANDLER.qualifier) {
            val jwtAuthenticationProvider: FirebaseIdTokenAuthenticationProvider = get(FIREBASE_ID_TOKEN_JWT_AUTH_PROVIDER.qualifier)
            JWTAuthHandler.create(jwtAuthenticationProvider)
        }

        single(FIREBASE_ID_TOKEN_KID_PUBLIC_KEYS.qualifier, createdAtStart = true) {
            val webClient: WebClient = getKoin().get(VERTX_WEB_CLIENT.qualifier)

            runBlocking {
                withContext(Dispatchers.Default) {
                    webClient
                        .get(idTokenKidPublicKeysPort, idTokenKidPublicKeysHost, idTokenKidPublicKeysUri)
                        .ssl(true)
                        .send()
                        .map { response ->
                            if (response.statusCode() != HttpResponseStatus.OK.code()) throw RuntimeException("No public keys where loaded. Can't validate any idTokens...")

                            response.bodyAsJsonObject().map
                        }
                        .map { publicKeys -> publicKeys.map { pubSecKeyOptionsOf(algorithm = "RS256", id = it.key, publicKey = it.value.toString(), buffer = it.value.toString()) } }
                        .await()
                }
            }
        }
    }
        .toKoinModuleWithOrder(moduleName = "firebaseIdTokenAuthOrderedKoinModule")

    override val koinOrderedModules: LinkedHashSet<KoinModuleWithOrder> =
        linkedSetOf(firebaseIdTokenAuthOrderedKoinModule)
            .apply { addAll(VertxWebclientKoinModule().koinOrderedModules) }

    companion object {
        private const val idTokenKidPublicKeysPort = 443
        private const val idTokenKidPublicKeysHost = "www.googleapis.com"
        private const val idTokenKidPublicKeysUri = "/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com"
    }
}

enum class FirebaseIdTokenKoinQualifiers(val qualifier: StringQualifier) {
    FIREBASE_ID_TOKEN_AUTH_PROPERTIES("FIREBASE_ID_TOKEN_AUTH_PROPERTIES".qualifier()),
    FIREBASE_ID_TOKEN_JWT_AUTH_PROVIDER("FIREBASE_ID_TOKEN_JWT_AUTH_PROVIDER".qualifier()),
    FIREBASE_ID_TOKEN_JWT_AUTH_HANDLER("FIREBASE_ID_TOKEN_JWT_AUTH_HANDLER".qualifier()),
    FIREBASE_ID_TOKEN_KID_PUBLIC_KEYS("FIREBASE_ID_TOKEN_KID_PUBLIC_KEYS".qualifier())
}
