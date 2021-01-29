package de.codecentric.kotlin.logger

import de.codecentric.kotlin.logger.ConsoleColors.CYAN
import de.codecentric.kotlin.logger.ConsoleColors.CYAN_BACKGROUND_BRIGHT
import de.codecentric.kotlin.logger.ConsoleColors.GREEN_BACKGROUND_BRIGHT
import de.codecentric.kotlin.logger.ConsoleColors.RED_BACKGROUND_BRIGHT
import de.codecentric.kotlin.logger.ConsoleColors.TEXT_RESET
import de.codecentric.kotlin.logger.ConsoleColors.YELLOW_BACKGROUND_BRIGHT
import mu.KLogger
import java.util.logging.Level
import java.util.logging.Level.CONFIG
import java.util.logging.Level.FINE
import java.util.logging.Level.FINER
import java.util.logging.Level.FINEST
import java.util.logging.Level.INFO
import java.util.logging.Level.SEVERE
import java.util.logging.Level.WARNING

const val defaultStartLogTab = 10
const val defaultLogTabDelta = 0
const val verticalLogTabDelta = 2

fun KLogger.loggerWithTab(tabCountDelta: Int = 0, level: Level = FINE, message: () -> Any?) {
    val tabString = getTabString(tabCountDelta)
    val textMessage = tabString.plus(message())
    writeLogByLevel(level, textMessage)
}

fun KLogger.loggerWithTab(color: String, tabCountDelta: Int = 0, level: Level = FINE, message: () -> Any?) {
    val tabString = getTabString(tabCountDelta)

    writeLogByLevel(level, "${color}${tabString}${message()}${TEXT_RESET}")
}

fun KLogger.infoWithTab(deltaTabCount: Int = 0, message: () -> Any?) = this.loggerWithTab(deltaTabCount, INFO, message)
fun KLogger.errorWithTab(deltaTabCount: Int = 0, message: () -> Any?) = this.loggerWithTab(deltaTabCount, SEVERE, message)

fun KLogger.positiveEvent(deltaTabCount: Int = 0, message: () -> Any?) = this.loggerWithTab(GREEN_BACKGROUND_BRIGHT, deltaTabCount, WARNING, message)
fun KLogger.neutralEvent(deltaTabCount: Int = 0, message: () -> Any?) = this.loggerWithTab(YELLOW_BACKGROUND_BRIGHT, deltaTabCount, WARNING, message)
fun KLogger.negativeEvent(deltaTabCount: Int = 0, message: () -> Any?) = this.loggerWithTab(RED_BACKGROUND_BRIGHT, deltaTabCount, WARNING, message)
fun KLogger.testEvent(deltaTabCount: Int = 0, message: () -> Any?) = this.loggerWithTab(CYAN_BACKGROUND_BRIGHT, deltaTabCount, WARNING, message)

suspend fun KLogger.loggerWithTabAsync(deltaTabCount: Int, level: Level = FINE, message: suspend () -> Any?) =
    loggerWithTab(deltaTabCount, level) { message }

private fun getTabString(deltaTabCount: Int): String {
    val logTab = when (deltaTabCount) {
        0 -> defaultStartLogTab
        in 0..Int.MAX_VALUE -> defaultStartLogTab - deltaTabCount
        in Int.MIN_VALUE..0 -> defaultStartLogTab + deltaTabCount
        else -> defaultStartLogTab
    }

    return logTab.toString().plus((1..logTab).joinToString(separator = "") { "----" })
}

private fun KLogger.writeLogByLevel(level: Level, message: String) {
    when (level.intValue()) {
        SEVERE.intValue() -> this.error(message)
        WARNING.intValue() -> this.warn(message)
        INFO.intValue(), CONFIG.intValue() -> this.info(message)
        FINE.intValue(), FINER.intValue() -> this.debug(message)
        FINEST.intValue() -> this.trace(message)
    }
}