package de.codecentric.kotlin.logger

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.util.logging.Level.INFO
import kotlin.reflect.full.starProjectedType

private val kLogger = KotlinLogging.logger { }

class ConsoleColorsTest {
    @Test
    fun `should include color details`() {
        ConsoleColors::class.members
            .filter { it.returnType == String::class.starProjectedType }
            .filter { it.name != "toString" }
            .forEach { member ->
                try {
                    kLogger.loggerWithTab((member.call().toString()), 0, INFO) { " x( ${member.name}" }
                } catch (th: Throwable) {
                    println("Error with member: ${member.name}")
                }
            }
    }
}