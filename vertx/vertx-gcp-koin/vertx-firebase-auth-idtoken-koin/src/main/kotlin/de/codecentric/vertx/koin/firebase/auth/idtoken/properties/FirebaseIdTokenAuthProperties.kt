package de.codecentric.vertx.koin.firebase.auth.idtoken.properties

import de.codecentric.vertx.koin.core.properties.ApplicationProperties
import de.codecentric.vertx.koin.core.properties.ProfileProperties
import io.vertx.ext.auth.JWTOptions

data class FirebaseIdTokenAuthApplicationProperties(
    override val applicationName: String,
    override val activeProfile: String,
    override var profiles: List<FirebaseIdTokenAuthProperties>
) : ApplicationProperties<FirebaseIdTokenAuthProperties>

data class FirebaseIdTokenAuthProperties(
    override var profile: String,
    var jwtOptions: JWTOptions
) : ProfileProperties
