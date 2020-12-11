package de.codecentric.koin.core

import org.koin.core.module.Module
import org.koin.dsl.ModuleDeclaration
import org.koin.dsl.module

class KoinModuleWithOrder(
    val order: Int = Int.MIN_VALUE,
    val moduleName: String,
    createdAtStart: Boolean = false,
    override: Boolean = false,
    moduleDeclaration: ModuleDeclaration? = null,
    val module: Module = module(createdAtStart, override, moduleDeclaration!!)
) {
    override fun toString(): String = StringBuilder()
        .appendLine()
        .append(" -> [$moduleName with order: $order")
        .toString()
}

fun Module.toKoinModuleWithOrder(order: Int = Int.MAX_VALUE, moduleName: String): KoinModuleWithOrder = KoinModuleWithOrder(order, moduleName, module = this)
