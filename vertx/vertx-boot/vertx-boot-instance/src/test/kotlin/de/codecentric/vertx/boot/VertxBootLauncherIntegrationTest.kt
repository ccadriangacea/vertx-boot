@file:Suppress("unused")

package de.codecentric.vertx.boot

import de.codecentric.util.fnresult.map
import de.codecentric.vertx.boot.verticle.KoinCoroutineVerticle
import de.codecentric.vertx.boot.verticle.logEndOfStart
import de.codecentric.vertx.koin.core.module.ConfigStoreType
import de.codecentric.vertx.koin.core.module.ConfigStoreType.ENV_VAR
import de.codecentric.vertx.koin.core.module.ConfigStoreType.FILE
import de.codecentric.vertx.koin.core.module.VertxConfigCommonModule.getActiveProfileProperty
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_CONFIG_RETRIEVER
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_CONFIG_STORE_TYPE_LIST
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_PROFILE_PROPERTIES
import de.codecentric.vertx.koin.core.properties.AbstractProfileProperties
import de.codecentric.vertx.koin.core.properties.DefaultApplicationProperties
import de.codecentric.vertx.koin.core.properties.VertxProfileProperties
import de.codecentric.vertx.koin.test.VertxLauncherIntegrationTest
import de.codecentric.vertx.koin.test.extension.KoinVertxExtension
import de.codecentric.vertx.koin.test.extension.softly
import io.vertx.config.ConfigRetriever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.get
import org.koin.core.component.inject

internal class BootVertxMainVerticle : KoinCoroutineVerticle() {
    private val configStoreTypeList: List<ConfigStoreType> by inject(VERTX_CONFIG_STORE_TYPE_LIST.qualifier)
    private val configRetriever: ConfigRetriever by inject(VERTX_CONFIG_RETRIEVER.qualifier)
    private val vertxProfileProperties: VertxProfileProperties by inject(VERTX_PROFILE_PROPERTIES.qualifier)

    override suspend fun start() {
        super.start()

        assertThat(configStoreTypeList).isNotEmpty
        assertThat(configStoreTypeList.size).isEqualTo(2)
        assertThat(configStoreTypeList).containsExactlyInAnyOrder(FILE, ENV_VAR)

        assertThat(configRetriever).isNotNull

        assertThat(vertxProfileProperties).isNotNull

        logEndOfStart()
    }
}

internal class VertxBootLauncherIntegrationTest : VertxLauncherIntegrationTest() {
    companion object {
        private val launcher = VertxBootLauncher(emptyArray())
            .apply { mainVerticleClass = "de.codecentric.vertx.boot.BootVertxMainVerticle" }

        @JvmField
        @RegisterExtension
        val koinVertxExtension = KoinVertxExtension(launcher)
    }

    @Test
    fun `should deploy main verticle on vertx`() {
        assertThat(vertx.deploymentIDs().size).isEqualTo(1)
    }

    @Test
    fun `should get VERTX_CONFIG_STORE_TYPE_LIST instance`() {
        val configStoreTypeList: List<ConfigStoreType> = getKoin().get(VERTX_CONFIG_STORE_TYPE_LIST.qualifier)

        softly {
            assertThat(configStoreTypeList).isNotEmpty
            assertThat(configStoreTypeList.size).isEqualTo(2)
            assertThat(configStoreTypeList).containsExactlyInAnyOrder(FILE, ENV_VAR)
        }
    }

    @Test
    fun `should get VERTX_CONFIG_RETRIEVER instance`() {
        val configRetriever = getKoin().get<ConfigRetriever>(VERTX_CONFIG_RETRIEVER.qualifier)

        assertThat(configRetriever).isNotNull
    }

    @Test
    fun `should get VERTX_MAIN_VERTICLE_OPTIONS instance`() {
        val vertxProfileProperties: VertxProfileProperties = getKoin().get(VERTX_PROFILE_PROPERTIES.qualifier)

        assertThat(vertxProfileProperties).isNotNull
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "activeProfile", matches = "test")
    fun `should load active profile`() {
        val configRetriever: ConfigRetriever = get(VERTX_CONFIG_RETRIEVER.qualifier)

        getActiveProfileProperty<DefaultApplicationProperties, AbstractProfileProperties>(configRetriever)
            .map { assertThat(it.profile).isEqualTo("test") }
    }
}