package de.codecentric.vertx.koin.core.module

import de.codecentric.koin.test.extension.DefaultKoinTest
import de.codecentric.koin.test.extension.KoinExtension
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.json.jackson.DatabindCodec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.annotation.KoinInternal

@OptIn(KoinInternal::class)
class VertxKoinModuleTest : DefaultKoinTest() {
    companion object {
        @JvmField
        @RegisterExtension
        val koinExtension = KoinExtension(VertxKoinModule().koinOrderedModules.toList())
    }

    @Test
    fun `should find koin definitions`() {
        val vertxDefinition = koin.scopeRegistry.rootScope._scopeDefinition.definitions.firstOrNull { it.qualifier == VERTX_INSTANCE.qualifier }
        assertThat(vertxDefinition).isNotNull
    }

    @Test
    fun `should get vertx instance from koin`() {
        val vertx = koin.get<Vertx>(VERTX_INSTANCE.qualifier)

        assertThat(vertx).isNotNull
    }

    @Test
    fun `should find jackson kotlin module on object mapper in vertx`() {
        val kotlinJacksonModule = DatabindCodec.mapper().registeredModuleIds.firstOrNull { it == "com.fasterxml.jackson.module.kotlin.KotlinModule" }

        assertThat(kotlinJacksonModule).isNotNull
    }

    @Test
    fun `should use kotlinJacksonModule features`() {
        val testDto = TestDto("id", "value")
        val testDtoToJson = JsonObject.mapFrom(testDto)
        val testDtoFromJson = JsonObject(testDtoStringValue)

        assertThat(testDtoToJson).isEqualTo(testDtoFromJson)
    }
}

private data class TestDto(
    val id: String,
    val key: String
)

private val testDtoStringValue = """{"id":"id","key":"value"}"""