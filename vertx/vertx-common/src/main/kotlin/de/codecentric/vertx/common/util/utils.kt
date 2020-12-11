package de.codecentric.vertx.common.util

import kotlin.system.exitProcess

private const val EXIT_CODE_KILL: Int = 42

fun doNothing() = Unit

fun killProcess(): Nothing = exitProcess(EXIT_CODE_KILL)