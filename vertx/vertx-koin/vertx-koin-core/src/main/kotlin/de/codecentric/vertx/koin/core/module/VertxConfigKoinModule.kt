package de.codecentric.vertx.koin.core.module

import de.codecentric.koin.core.KoinModule
import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.koin.core.qualifier
import de.codecentric.koin.core.toKoinModuleWithOrder
import de.codecentric.util.fnresult.FnResult
import de.codecentric.util.fnresult.getResult
import de.codecentric.util.fnresult.handleThrowableAsync
import de.codecentric.util.fnresult.map
import de.codecentric.util.fnresult.onFailure
import de.codecentric.vertx.koin.core.exception.ConfigurationLoadingException
import de.codecentric.vertx.koin.core.module.ConfigStoreType.DIRECTORY
import de.codecentric.vertx.koin.core.module.ConfigStoreType.ENV_VAR
import de.codecentric.vertx.koin.core.module.ConfigStoreType.EVENT_BUS
import de.codecentric.vertx.koin.core.module.ConfigStoreType.FILE
import de.codecentric.vertx.koin.core.module.ConfigStoreType.FILE_SECRET
import de.codecentric.vertx.koin.core.module.ConfigStoreType.HTTP
import de.codecentric.vertx.koin.core.module.ConfigStoreType.JSON
import de.codecentric.vertx.koin.core.module.ConfigStoreType.SYSTEM_PROP
import de.codecentric.vertx.koin.core.module.VertxConfigCommonModule.getActiveProfileProperty
import de.codecentric.vertx.koin.core.module.VertxConfigCommonModule.getApplicationProperties
import de.codecentric.vertx.koin.core.module.VertxConfigCommonModule.getConfigRetriever
import de.codecentric.vertx.koin.core.module.VertxConfigCommonModule.getConfigStoreOptions
import de.codecentric.vertx.koin.core.module.VertxConfigCommonModule.getConfigurationSource
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_APPLICATION_PROPERTIES
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_CONFIG_RETRIEVER
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_CONFIG_STORE_TYPE_LIST
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_PROFILE_PROPERTIES
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
import de.codecentric.vertx.koin.core.properties.ApplicationProperties
import de.codecentric.vertx.koin.core.properties.ProfileProperties
import de.codecentric.vertx.koin.core.properties.VertxApplicationProperties
import de.codecentric.vertx.koin.core.properties.VertxProfileProperties
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Vertx
import io.vertx.core.file.impl.FileResolver
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

class VertxConfigKoinModule : KoinModule {
    private val vertxConfigKoinOrderedModule = module {
        single(VERTX_CONFIG_STORE_TYPE_LIST.qualifier) { listOf(FILE, ENV_VAR) }

        single(VERTX_CONFIG_RETRIEVER.qualifier) {
            val vertx: Vertx = get(VERTX_INSTANCE.qualifier)

            val configStoreTypeList: List<ConfigStoreOptions> = get<List<ConfigStoreType>>(VERTX_CONFIG_STORE_TYPE_LIST.qualifier)
                .map { getConfigStoreOptions(it, getConfigurationSource(it)) }

            getConfigRetriever(vertx, configStoreTypeList)
        }

        single(VERTX_APPLICATION_PROPERTIES.qualifier) {
            val configRetriever: ConfigRetriever = get(VERTX_CONFIG_RETRIEVER.qualifier)

            getApplicationProperties<VertxApplicationProperties>(configRetriever).getResult()
        }

        single(VERTX_PROFILE_PROPERTIES.qualifier) {
            val configRetriever: ConfigRetriever = get(VERTX_CONFIG_RETRIEVER.qualifier)

            getActiveProfileProperty<VertxApplicationProperties, VertxProfileProperties>(configRetriever).getResult()
        }
    }.toKoinModuleWithOrder(moduleName = "vertxConfigKoinOrderedModule")

    override val koinOrderedModules: LinkedHashSet<KoinModuleWithOrder> = linkedSetOf(vertxConfigKoinOrderedModule)
}

object VertxConfigCommonModule {
    private const val DEFAULT_CONFIG_FILE_NAME = "application.json"
    private const val DEFAULT_CONFIG_FILE_NAME_SECRET = "application-secret.json"
    private const val DEFAULT_CONFIG_DIRECTORY_NAME = "configs"
    private const val DEFAULT_EVENT_BUS_CONFIGURATION_ADDRESS = "event-bus-config-source"

    fun getConfigurationSource(configStoreType: ConfigStoreType): String =
        when (configStoreType) {
            FILE -> DEFAULT_CONFIG_FILE_NAME
            FILE_SECRET -> DEFAULT_CONFIG_FILE_NAME_SECRET
            DIRECTORY -> FileResolver().resolveFile(DEFAULT_CONFIG_DIRECTORY_NAME).absolutePath
            ENV_VAR, JSON, SYSTEM_PROP, HTTP, EVENT_BUS -> "empty"
        }

    fun getConfigStoreOptions(configStoreType: ConfigStoreType, configurationFile: String): ConfigStoreOptions = ConfigStoreOptions()
        .setType(configStoreType.configStoreOptionsType)
        .also {
            when (configStoreType) {
                FILE -> it.setFormat("json").config = JsonObject(mapOf("path" to configurationFile))
                FILE_SECRET -> it.setFormat("json").config = JsonObject(mapOf("path" to configurationFile))
                SYSTEM_PROP -> it.config = JsonObject(mapOf("cache" to false))
                EVENT_BUS -> it.config = JsonObject(mapOf("address" to DEFAULT_EVENT_BUS_CONFIGURATION_ADDRESS))
                DIRECTORY -> it.config = JsonObject(mapOf("path" to configurationFile, "filesets" to JsonArray().add(JsonObject(mapOf("pattern" to "*.json")))))
                ENV_VAR -> it.config = JsonObject(mapOf("raw-data" to true))
                JSON -> it.config = JsonObject(mapOf("key" to "value"))
                HTTP -> it.config = JsonObject(mapOf("host" to "localhost", "port" to 8888, "path" to "/config"))
            }
        }

    fun getConfigRetriever(vertx: Vertx, configStoreOptions: List<ConfigStoreOptions>): ConfigRetriever {
        val configRetrieverOptions = ConfigRetrieverOptions()
        configStoreOptions.forEach { configRetrieverOptions.addStore(it) }
        return ConfigRetriever.create(vertx, configRetrieverOptions)
    }

    inline fun <reified A : ApplicationProperties<*>> getApplicationProperties(configRetriever: ConfigRetriever): FnResult<A> =
        runBlocking {
            withContext(Dispatchers.Default) {
                handleThrowableAsync { configRetriever.config.await() }
                    .map { config -> config.mapTo(A::class.java) }
                    .onFailure {
                        System.err.println("Exception trying to getApplicationProperties: ${it.errorMessage} -> Cause: ${it.cause}")
                        FnResult.FnError(ConfigurationLoadingException("Error trying to load config ${A::class}"))
                    }
            }
        }

    inline fun <reified A : ApplicationProperties<*>, reified P : ProfileProperties> getActiveProfileProperty(configRetriever: ConfigRetriever): FnResult<P> =
        // TODO check if using runBlocking is helpful: should be because is not blocking the thread with io operations and because of await functions that are used
        runBlocking {
            withContext(Dispatchers.Default) {
                handleThrowableAsync { configRetriever.config.await() }
                    .map { config -> config.mapTo(A::class.java) }
                    .map { applicationProperties ->
                        applicationProperties
                            .profiles
                            .map { it as ProfileProperties }
                            .firstOrNull { it.profile == applicationProperties.activeProfile }
                            ?: throw ConfigurationLoadingException("ActiveProfile[${applicationProperties.activeProfile}] is not configured!")
                    }
                    .map { it as P }
                    .onFailure {
                        System.err.println("Exception trying to getActiveProfileProperty: ${it.errorMessage} -> Cause: ${it.cause}")
                        FnResult.FnError(ConfigurationLoadingException("Error trying to load config ${A::class}"))
                    }
            }
        }
}

enum class ConfigStoreType(val configStoreOptionsType: String) {
    FILE("file"),
    FILE_SECRET("file"),
    JSON("json"),
    ENV_VAR("env"),
    SYSTEM_PROP("sys"),
    HTTP("http"),
    EVENT_BUS("event-bus"),
    DIRECTORY("directory")
}

enum class ClusterVertxKoinQualifiers(val qualifier: StringQualifier) {
    CLUSTER_VERTX_CLUSTER_MANAGER("CLUSTER_VERTX_CLUSTER_MANAGER".qualifier()),
    CLUSTER_VERTX_CLUSTER_MANAGER_FROM_FACTORY("CLUSTER_VERTX_CLUSTER_MANAGER_FROM_FACTORY".qualifier());
}

enum class VertxConfigKoinQualifiers(val qualifier: StringQualifier) {
    VERTX_CONFIG_STORE_TYPE_LIST("VERTX_CONFIG_STORE_TYPE_LIST".qualifier()),
    VERTX_CONFIG_RETRIEVER("VERTX_CONFIG_RETRIEVER".qualifier()),
    VERTX_APPLICATION_PROPERTIES("VERTX_APPLICATION_PROPERTIES".qualifier()),
    VERTX_PROFILE_PROPERTIES("VERTX_PROFILE_PROPERTIES".qualifier())
}
