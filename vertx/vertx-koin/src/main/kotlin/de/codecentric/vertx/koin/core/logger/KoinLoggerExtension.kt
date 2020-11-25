package de.codecentric.vertx.koin.core.logger

import org.koin.core.logger.Logger
import java.util.logging.Level

fun Logger.loggerWithTab(tabCount: Int, level: Level = Level.FINE, msg: () -> Any?) {
    val tabString = getTabString(tabCount)

    when (level.intValue()) {
        Level.SEVERE.intValue() -> this.error(tabString.plus(msg()))
        Level.WARNING.intValue(), Level.INFO.intValue(), Level.CONFIG.intValue() -> this.info(tabString.plus(msg()))
        else -> this.debug(tabString.plus(msg()))
    }
}

private fun getTabString(tabCount: Int): String = if (tabCount > 0) tabCount.toString().plus((1..tabCount).joinToString(separator = "") { "----" }) else ""