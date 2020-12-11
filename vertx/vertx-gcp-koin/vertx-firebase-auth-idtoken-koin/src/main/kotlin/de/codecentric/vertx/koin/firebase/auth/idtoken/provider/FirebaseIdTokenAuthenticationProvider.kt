package de.codecentric.vertx.koin.firebase.auth.idtoken.provider

import de.codecentric.koin.core.KoinComponentWithOptIn
import de.codecentric.util.fnresult.handleThrowable
import de.codecentric.util.fnresult.map
import de.codecentric.util.fnresult.onFailureEmpty
import de.codecentric.vertx.koin.web.exception.ForbiddenException
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.User
import io.vertx.ext.auth.authentication.Credentials
import io.vertx.ext.auth.impl.jose.JWK
import io.vertx.ext.auth.impl.jose.JWT
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import java.time.Instant
import java.util.Collections

class FirebaseIdTokenAuthenticationProvider(private val jwtAuthOptions: JWTAuthOptions) : JWTAuth, KoinComponentWithOptIn {
    private val logger = getKoin().logger

    private val jwt: JWT = JWT().apply {
        jwtAuthOptions.pubSecKeys.forEach { pubSecKeyOptions ->
            val jwk = JWK(pubSecKeyOptions)
            this.addJWK(jwk)
        }
    }

    override fun authenticate(credentials: JsonObject, resultHandler: Handler<AsyncResult<User>>) {
        logger.debug("authenticate json: $credentials")

        resultHandler.handle(authenticate { credentials })
    }

    override fun authenticate(credentials: Credentials): Future<User> {
        logger.debug("authenticate jwt: $credentials")

        val result = Promise.promise<User>()
        handleThrowable { jwt.decode(credentials.toString()) }
            .map { payload ->
                logger.debug("jwt payload:\n${payload.encodePrettily()}")

                val now = Instant.now()

                if (Instant.ofEpochMilli(payload.getLong("exp")).isBefore(now)) {
                    logger.error("Expired JWT token.")
                    throw ForbiddenException("Expired JWT token.")
                }

                val iss = payload.getString("iss")
                if (iss != jwtAuthOptions.jwtOptions.issuer) {
                    logger.error("Wrong Issuer: $iss")
                }

                val aud = listOf<String>(payload.getString("aud"))
                if (Collections.disjoint(jwtAuthOptions.jwtOptions.audience, aud)) {
                    logger.error("Wrong Audience token: $aud")
                    throw ForbiddenException("Wrong Audience token.")
                }

                if (payload.getString("sub").isBlank()) {
                    logger.error("No subject found...")
                    throw ForbiddenException("No subject found in token.")
                }


                if (Instant.ofEpochMilli(payload.getLong("auth_time")).isAfter(now) ||
                    Instant.ofEpochMilli(payload.getLong("iat")).isAfter(now)
                ) {
                    logger.error("Issued-at-time/Authentication time is not in the past")
                    throw ForbiddenException("Issued-at-time/Authentication time is not in the past")
                }

                val user = User.create(json {
                    obj(
                        "uid" to payload.getString("sub"),
                        "name" to payload.getString("name"),
                        "email" to payload.getString("email"),
                        "email_verified" to payload.getBoolean("email_verified"),
                        "expired" to payload.getLong("exp")
                    )
                })

                result.complete(user)
            }
            .onFailureEmpty {
                logger.error("Exception trying to authenticate jwt token.")
                result.fail(ForbiddenException("IdToken is not valid!"))
            }

        return result.future()
    }

    override fun generateToken(claims: JsonObject, options: JWTOptions): String {
        TODO("Not supported... go to login?")
    }
}
