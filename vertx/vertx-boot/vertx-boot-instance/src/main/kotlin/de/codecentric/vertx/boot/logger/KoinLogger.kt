package de.codecentric.vertx.boot.logger

import mu.KLogger
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

class KoinLogger(private val kLogger: KLogger) : Logger(Level.INFO) {
    override fun log(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> kLogger.debug { msg }
            Level.INFO -> kLogger.info(msg)
            Level.ERROR -> kLogger.error(msg)
            else -> println("$level:$msg")
        }
    }
}
