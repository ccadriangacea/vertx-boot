package de.codecentric.vertx.mongo.module

import de.codecentric.koin.core.KoinModule
import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.koin.core.qualifier
import de.codecentric.koin.core.toKoinModuleWithOrder
import de.codecentric.util.fnresult.FnResult
import de.codecentric.util.fnresult.getResult
import de.codecentric.util.fnresult.handleThrowable
import de.codecentric.util.fnresult.map
import de.codecentric.util.fnresult.onFailure
import de.codecentric.vertx.koin.core.module.VertxConfigCommonModule.getActiveProfileProperty
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_CONFIG_RETRIEVER
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
import de.codecentric.vertx.mongo.module.VertxMongodbKoinQualifiers.VERTX_MONGODB_CLIENT_NON_SHARED_POOL
import de.codecentric.vertx.mongo.module.VertxMongodbKoinQualifiers.VERTX_MONGODB_CLIENT_OPTIONS
import de.codecentric.vertx.mongo.module.VertxMongodbKoinQualifiers.VERTX_MONGODB_CLIENT_SHARED_POOL
import de.codecentric.vertx.mongo.properties.MongodbApplicationProperties
import de.codecentric.vertx.mongo.properties.MongodbProfileProperties
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

class VertxMongodbKoinModule : KoinModule {
    private val vertxMongodbOrderedKoinModule = module {
        single(VERTX_MONGODB_CLIENT_OPTIONS.qualifier) {
            System.setProperty("org.mongodb.async.type", "netty")
            // System.setProperty("javax.net.debug", "ssl")

            val configRetriever: ConfigRetriever = get(VERTX_CONFIG_RETRIEVER.qualifier)

            getActiveProfileProperty<MongodbApplicationProperties, MongodbProfileProperties>(configRetriever)
                .map { JsonObject(it.mongodbClientOptions) }
                .getResult()
        }

        single(VERTX_MONGODB_CLIENT_SHARED_POOL.qualifier) {
            val vertx: Vertx = get(VERTX_INSTANCE.qualifier)
            val config: JsonObject = get(VERTX_MONGODB_CLIENT_OPTIONS.qualifier)

            handleThrowable { MongoClient.createShared(vertx, config) }
                .onFailure { FnResult.FnSuccess(null) }
                .getResult()
        }

        single(VERTX_MONGODB_CLIENT_NON_SHARED_POOL.qualifier) {
            val vertx: Vertx = get(VERTX_INSTANCE.qualifier)
            val config: JsonObject = get(VERTX_MONGODB_CLIENT_OPTIONS.qualifier)

            MongoClient.create(vertx, config)
        }
    }.toKoinModuleWithOrder(moduleName = "vertxMongodbOrderedKoinModule")

    override val koinOrderedModules: LinkedHashSet<KoinModuleWithOrder> = linkedSetOf(vertxMongodbOrderedKoinModule)
}

enum class VertxMongodbKoinQualifiers(val qualifier: StringQualifier) {
    VERTX_MONGODB_CLIENT_OPTIONS("VERTX_MONGODB_CLIENT_OPTIONS".qualifier()),
    VERTX_MONGODB_CLIENT_SHARED_POOL("VERTX_MONGODB_CLIENT_SHARED_POOL".qualifier()),
    VERTX_MONGODB_CLIENT_NON_SHARED_POOL("VERTX_MONGODB_CLIENT_NON_SHARED_POOL".qualifier())
    ;
}
