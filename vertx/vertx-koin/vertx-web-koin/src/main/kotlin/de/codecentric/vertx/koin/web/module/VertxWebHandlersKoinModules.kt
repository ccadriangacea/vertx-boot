package de.codecentric.vertx.koin.web.module

import de.codecentric.koin.core.KoinModule
import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.koin.core.qualifier
import de.codecentric.koin.core.toKoinModuleWithOrder
import de.codecentric.util.fnresult.getResult
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_CONFIG_RETRIEVER
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
import de.codecentric.vertx.koin.web.auth.LocalAuthenticationProvider
import de.codecentric.vertx.koin.web.module.VertxHttpServerKoinQualifiers.VERTX_HTTPSERVER_SECURITY_OPTIONS_DEFAULT_PORT
import de.codecentric.vertx.koin.web.module.VertxWebCommonModule.getSecurityOptions
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinQualifiers.VERTX_HTTPSERVER_BODY_HANDLER
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinQualifiers.VERTX_HTTPSERVER_CORS_HANDLER
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinQualifiers.VERTX_HTTPSERVER_JWT_AUTHENTICATION_HANDLER
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinQualifiers.VERTX_HTTPSERVER_LOCAL_PASSWORD_AUTHENTICATION_HANDLER
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinQualifiers.VERTX_HTTPSERVER_OAUTH_CODE_AUTHENTICATION_HANDLER
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinQualifiers.VERTX_HTTPSERVER_OAUTH_PASSWORD_AUTHENTICATION_HANDLER
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinQualifiers.VERTX_HTTPSERVER_PERMISSION_BASED_AUTHORIZATION_HANDLER
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinQualifiers.VERTX_HTTPSERVER_ROLE_BASED_AUTHORIZATION_HANDLER
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinQualifiers.VERTX_HTTPSERVER_SESSION_HANDLER
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.http.CookieSameSite
import io.vertx.core.http.HttpMethod.DELETE
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.OPTIONS
import io.vertx.core.http.HttpMethod.POST
import io.vertx.core.http.HttpMethod.PUT
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization
import io.vertx.ext.auth.authorization.RoleBasedAuthorization
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.auth.oauth2.OAuth2Auth
import io.vertx.ext.auth.oauth2.OAuth2FlowType.AUTH_CODE
import io.vertx.ext.auth.oauth2.OAuth2FlowType.PASSWORD
import io.vertx.ext.auth.oauth2.OAuth2Options
import io.vertx.ext.web.handler.AuthorizationHandler
import io.vertx.ext.web.handler.BasicAuthHandler
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.ext.web.handler.OAuth2AuthHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.ClusteredSessionStore
import io.vertx.ext.web.sstore.SessionStore
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import java.util.LinkedHashSet

open class VertxWebHandlersKoinModules : KoinModule {
    private val vertxWebHandlersOrderedKoinModule = module {
        single(VERTX_HTTPSERVER_SECURITY_OPTIONS_DEFAULT_PORT.qualifier) {
            val configRetriever: ConfigRetriever = get(VERTX_CONFIG_RETRIEVER.qualifier)

            getSecurityOptions(configRetriever).getResult()
        }

        single(VERTX_HTTPSERVER_BODY_HANDLER.qualifier) {
            BodyHandler.create()
        }

        single(VERTX_HTTPSERVER_SESSION_HANDLER.qualifier) {
            val store: SessionStore = ClusteredSessionStore.create(get(VERTX_INSTANCE.qualifier))
            SessionHandler.create(store).apply {
                setCookieSameSite(CookieSameSite.STRICT)
            }
        }

        single(VERTX_HTTPSERVER_LOCAL_PASSWORD_AUTHENTICATION_HANDLER.qualifier) {
            val securityOptions: Map<String, String> = get(VERTX_HTTPSERVER_SECURITY_OPTIONS_DEFAULT_PORT.qualifier)

            LocalAuthenticationProvider(securityOptions)
        }

        single(VERTX_HTTPSERVER_OAUTH_PASSWORD_AUTHENTICATION_HANDLER.qualifier) {
            val oAuth2Auth = OAuth2Auth.create(get(VERTX_INSTANCE.qualifier), OAuth2Options().apply { flow = PASSWORD })
            BasicAuthHandler.create(oAuth2Auth, "vertx-boot-realm")
        }

        single(VERTX_HTTPSERVER_OAUTH_CODE_AUTHENTICATION_HANDLER.qualifier) { (callbackURL: String) ->
            val vertx = get<Vertx>(VERTX_INSTANCE.qualifier)
            val oAuth2Auth = OAuth2Auth.create(vertx, OAuth2Options().apply { flow = AUTH_CODE })
            OAuth2AuthHandler.create(vertx, oAuth2Auth, callbackURL)
        }

        single(VERTX_HTTPSERVER_JWT_AUTHENTICATION_HANDLER.qualifier) {
            val jwtAuth = JWTAuth.create(get(VERTX_INSTANCE.qualifier), JWTAuthOptions().apply { })
            JWTAuthHandler.create(jwtAuth)
        }

        single(VERTX_HTTPSERVER_PERMISSION_BASED_AUTHORIZATION_HANDLER.qualifier) { (permission: String) ->
            val permissionBasedAuthorization = PermissionBasedAuthorization.create(permission)
            AuthorizationHandler.create(permissionBasedAuthorization)
        }

        single(VERTX_HTTPSERVER_ROLE_BASED_AUTHORIZATION_HANDLER.qualifier) { (role: String) ->
            val roleBasedAuthorization = RoleBasedAuthorization.create(role)
            AuthorizationHandler.create(roleBasedAuthorization)
        }

        single(VERTX_HTTPSERVER_CORS_HANDLER.qualifier) { (allowedOriginPattern: String?) ->
            CorsHandler.create(allowedOriginPattern ?: "http://localhost:8080")
                .allowedMethod(GET)
                .allowedMethod(POST)
                .allowedMethod(PUT)
                .allowedMethod(DELETE)
                .allowedMethod(OPTIONS)
                .allowCredentials(true)
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Authorization")
                .allowedHeader("Access-Control-Allow-Method")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Content-Type")
        }
    }.toKoinModuleWithOrder(moduleName = "vertxWebHandlersOrderedKoinModule")

    override val koinOrderedModules: LinkedHashSet<KoinModuleWithOrder> = linkedSetOf(vertxWebHandlersOrderedKoinModule)
}

enum class VertxWebHandlersKoinQualifiers(val qualifier: StringQualifier) {
    VERTX_HTTPSERVER_BODY_HANDLER("VERTX_HTTPSERVER_BODY_HANDLER".qualifier()),
    VERTX_HTTPSERVER_CORS_HANDLER("VERTX_HTTPSERVER_CORS_HANDLER".qualifier()),
    VERTX_HTTPSERVER_SESSION_HANDLER("VERTX_HTTPSERVER_SESSION_HANDLER".qualifier()),
    VERTX_HTTPSERVER_LOCAL_PASSWORD_AUTHENTICATION_HANDLER("VERTX_HTTPSERVER_LOCAL_PASSWORD_AUTHENTICATION_HANDLER".qualifier()),
    VERTX_HTTPSERVER_OAUTH_PASSWORD_AUTHENTICATION_HANDLER("VERTX_HTTPSERVER_OAUTH_PASSWORD_AUTHENTICATION_HANDLER".qualifier()),
    VERTX_HTTPSERVER_OAUTH_CODE_AUTHENTICATION_HANDLER("VERTX_HTTPSERVER_OAUTH_CODE_AUTHENTICATION_HANDLER".qualifier()),
    VERTX_HTTPSERVER_JWT_AUTHENTICATION_HANDLER("VERTX_HTTPSERVER_JWT_AUTHENTICATION_HANDLER".qualifier()),
    VERTX_HTTPSERVER_PERMISSION_BASED_AUTHORIZATION_HANDLER("VERTX_HTTPSERVER_PERMISSION_BASED_AUTHORIZATION_HANDLER".qualifier()),
    VERTX_HTTPSERVER_ROLE_BASED_AUTHORIZATION_HANDLER("VERTX_HTTPSERVER_ROLE_BASED_AUTHORIZATION_HANDLER".qualifier())
}
