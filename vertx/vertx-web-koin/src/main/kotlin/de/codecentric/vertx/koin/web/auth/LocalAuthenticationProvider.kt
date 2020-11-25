package de.codecentric.vertx.koin.web.auth

import io.vertx.core.AsyncResult
import io.vertx.core.Future.failedFuture
import io.vertx.core.Future.succeededFuture
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.authentication.AuthenticationProvider

class LocalAuthenticationProvider internal constructor(private val securityOptions: Map<String, String>) : AuthenticationProvider {
    override fun authenticate(credentials: JsonObject, resultHandler: Handler<AsyncResult<User>>) {
        val username = credentials.getString("username")
        val password = credentials.getString("password")

        if (securityOptions.containsKey(username)) {
            if (securityOptions[username] == password) {
                resultHandler.handle(succeededFuture(User.create(JsonObject(mapOf("username" to username, "password" to password)))))
            }
        }

        resultHandler.handle(failedFuture(Exception("Credentials are not correct!")))
    }
}
