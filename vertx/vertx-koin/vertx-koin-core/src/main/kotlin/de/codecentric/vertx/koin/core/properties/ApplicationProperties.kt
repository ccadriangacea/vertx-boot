package de.codecentric.vertx.koin.core.properties

// TODO this is to complicated to define every time?
interface ApplicationProperties<T> {
    val applicationName: String
    val activeProfile: String
    val profiles: List<T>
}

interface ProfileProperties {
    val profile: String
}

const val DEFAULT_APPLICATION_NAME: String = "defaultVertxApplication"
const val DEFAULT_ACTIVE_PROFILE: String = "default"

abstract class AbstractApplicationProperties<ProfileProperties>(
    override val applicationName: String,
    override val activeProfile: String,
    override val profiles: List<ProfileProperties>
) : ApplicationProperties<ProfileProperties>

abstract class AbstractProfileProperties(override val profile: String) : ProfileProperties

data class DefaultApplicationProperties(
    override val applicationName: String,
    override val activeProfile: String,
    override var profiles: List<DefaultProfileProperties>
) : AbstractApplicationProperties<DefaultProfileProperties>(applicationName, activeProfile, profiles)

data class DefaultProfileProperties(override val profile: String) : AbstractProfileProperties(profile)
