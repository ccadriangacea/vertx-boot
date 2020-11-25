package de.codecentric.vertx.koin.core.module

import de.codecentric.vertx.koin.core.ModuleWithOrder
import org.koin.core.qualifier.StringQualifier

interface KoinModule {
    val koinOrderedModules: LinkedHashSet<ModuleWithOrder>
}

fun String.qualifier() = StringQualifier(this)
