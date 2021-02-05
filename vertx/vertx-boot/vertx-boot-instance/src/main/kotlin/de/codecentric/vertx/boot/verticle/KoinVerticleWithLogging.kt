package de.codecentric.vertx.boot.verticle

import de.codecentric.kotlin.logger.loggerWithTab
import de.codecentric.kotlin.logger.verticalLogTabDelta
import de.codecentric.vertx.koin.core.verticle.AbstractKoinCoroutineVerticle
import mu.KLogger
import mu.KotlinLogging

private val logger: KLogger = KotlinLogging.logger {}

abstract class KoinCoroutineVerticle : AbstractKoinCoroutineVerticle() {
    override suspend fun start() =
        logger.loggerWithTab(verticalLogTabDelta) { " -> start of ${this::class.simpleName ?: "Unknown"} [deploymentID=${this.deploymentID}]" }

    override suspend fun stop() {
        logger.loggerWithTab(verticalLogTabDelta) { " <- stop of ${this::class.simpleName ?: "Unknown"}" }
    }
}

fun AbstractKoinCoroutineVerticle.logEndOfStart() =
    logger.loggerWithTab(verticalLogTabDelta) { " <- starting of ${this::class.simpleName ?: "Unknown"} done!" }