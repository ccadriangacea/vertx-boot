package de.codecentric.vertx.koin.core.verticle

import de.codecentric.koin.core.KoinComponentWithOptIn
import io.vertx.kotlin.coroutines.CoroutineVerticle

abstract class AbstractKoinCoroutineVerticle : CoroutineVerticle(), KoinComponentWithOptIn