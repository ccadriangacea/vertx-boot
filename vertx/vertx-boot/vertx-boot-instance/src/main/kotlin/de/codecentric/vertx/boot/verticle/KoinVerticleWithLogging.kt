package de.codecentric.vertx.boot.verticle

import de.codecentric.vertx.koin.core.verticle.AbstractKoinCoroutineVerticle
import de.codecentric.vertx.koin.core.verticle.AbstractKoinVerticle
import de.codecentric.kotlin.logger.loggerWithTab
import mu.KLogger
import mu.KotlinLogging

private val logger: KLogger = KotlinLogging.logger {}

abstract class KoinVerticle : AbstractKoinVerticle() {
    override fun start() =
        logger.loggerWithTab(10) { " -> start of ${this::class.simpleName ?: "Unknown"} [deploymentID=${this.deploymentID()}]" }

    override fun stop() =
        logger.loggerWithTab(10) { " <- stop of ${this::class.simpleName ?: "Unknown"}" }
}

abstract class KoinCoroutineVerticle : AbstractKoinCoroutineVerticle() {
    override suspend fun start() =
        logger.loggerWithTab(10) { " -> start of ${this::class.simpleName ?: "Unknown"} [deploymentID=${this.deploymentID}]" }

    override suspend fun stop() {
        logger.loggerWithTab(10) { " <- stop of ${this::class.simpleName ?: "Unknown"}" }
    }
}

fun KoinCoroutineVerticle.logEndOfStart() =
    logger.loggerWithTab(10) { " <- starting of ${this::class.simpleName ?: "Unknown"} done!" }