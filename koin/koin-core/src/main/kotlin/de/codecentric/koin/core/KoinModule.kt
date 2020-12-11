package de.codecentric.koin.core

import org.koin.core.qualifier.StringQualifier

interface KoinModule {
    val koinOrderedModules: LinkedHashSet<KoinModuleWithOrder>
}

fun String.qualifier() = StringQualifier(this)
