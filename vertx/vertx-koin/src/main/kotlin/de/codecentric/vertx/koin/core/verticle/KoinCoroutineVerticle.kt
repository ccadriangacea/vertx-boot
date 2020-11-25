package de.codecentric.vertx.koin.core.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@OptIn(KoinApiExtension::class)
interface KoinComponentWithOptIn : KoinComponent

abstract class AbstractKoinVerticle : AbstractVerticle(), KoinComponentWithOptIn

abstract class AbstractKoinCoroutineVerticle : CoroutineVerticle(), KoinComponentWithOptIn
