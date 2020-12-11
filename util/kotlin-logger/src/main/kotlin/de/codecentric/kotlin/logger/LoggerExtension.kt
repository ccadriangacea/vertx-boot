package de.codecentric.kotlin.logger

import mu.KLogger
import java.util.logging.Level
import java.util.logging.Level.CONFIG
import java.util.logging.Level.FINE
import java.util.logging.Level.FINER
import java.util.logging.Level.INFO
import java.util.logging.Level.SEVERE
import java.util.logging.Level.WARNING

fun KLogger.loggerWithTab(tabCount: Int, level: Level = FINE, msg: () -> Any?) {
    val tabString = getTabString(tabCount)

    when (level.intValue()) {
        SEVERE.intValue() -> this.error(tabString.plus(msg()))
        WARNING.intValue() -> this.warn(tabString.plus(msg()))
        INFO.intValue(), CONFIG.intValue() -> this.info(tabString.plus(msg()))
        FINE.intValue(), FINER.intValue() -> this.debug { tabString.plus(msg()) }
        else -> this.trace(tabString.plus(msg()))
    }
}

suspend fun KLogger.loggerWithTabAsync(tabCount: Int, level: Level = FINE, msg: suspend () -> Any?) {
    val tabString = getTabString(tabCount)

    when (level.intValue()) {
        SEVERE.intValue() -> this.error(tabString.plus(msg()))
        WARNING.intValue() -> this.warn(tabString.plus(msg()))
        INFO.intValue(), CONFIG.intValue() -> this.info(tabString.plus(msg()))
        FINE.intValue(), FINER.intValue() -> this.debug(tabString.plus(msg()))
        else -> this.trace(tabString.plus(msg()))
    }
}

private fun getTabString(tabCount: Int): String = if (tabCount > 0) tabCount.toString().plus((1..tabCount).joinToString(separator = "") { "----" }) else ""