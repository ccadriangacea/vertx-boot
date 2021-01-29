package io.vertx.core.spi

import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.net.impl.transport.Transport

/**
 * Factory for creating Vertx instances.
 *
 * Use this to create Vertx instances when embedding Vert.x core directly.
 *
 * @author pidster
 */
// This is needed because camel still uses it and vert.x 4 removed it already
interface VertxFactory {
    fun vertx(): Vertx
    fun vertx(options: VertxOptions): Vertx
    fun vertx(options: VertxOptions, transport: Transport): Vertx
    fun clusteredVertx(options: VertxOptions, resultHandler: Handler<AsyncResult<Vertx>>)
    fun clusteredVertx(options: VertxOptions, transport: Transport, resultHandler: Handler<AsyncResult<Vertx>>)
    fun context(): Context
}