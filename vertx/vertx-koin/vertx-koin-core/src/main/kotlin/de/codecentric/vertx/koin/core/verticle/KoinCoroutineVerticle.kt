package de.codecentric.vertx.koin.core.verticle

import de.codecentric.koin.core.KoinComponentWithOptIn
import io.vertx.core.AbstractVerticle
import io.vertx.kotlin.coroutines.CoroutineVerticle

abstract class AbstractKoinVerticle : AbstractVerticle(), KoinComponentWithOptIn

abstract class AbstractKoinCoroutineVerticle : CoroutineVerticle(), KoinComponentWithOptIn