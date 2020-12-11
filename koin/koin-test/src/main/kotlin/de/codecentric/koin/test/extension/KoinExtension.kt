package de.codecentric.koin.test.extension

import de.codecentric.koin.core.KoinModuleWithOrder
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class KoinExtension(private val orderedModules: List<KoinModuleWithOrder>) : ParameterResolver, AfterEachCallback, TestInstancePostProcessor {
    private var _koin: Koin? = null

    val koin: Koin
        get() = _koin ?: error("No Koin application found")

    override fun afterEach(extensionContext: ExtensionContext) {
        stopKoin()
        _koin = null
    }

    override fun postProcessTestInstance(testInstance: Any, extensionContext: ExtensionContext) {
        if (_koin == null) {
            val koinApp = startKoin {
                modules(orderedModules.sortedBy { it.order }.map { it.module }.toList())
            }
            _koin = koinApp.koin
        }

        if (testInstance::class.isInner) {
            testInstance::class.supertypes
                .firstOrNull { it is DefaultKoinTest }
                ?.let { (testInstance as DefaultKoinTest).koin = _koin!! }
        } else {
            when (testInstance) {
                is DefaultKoinTest -> testInstance.koin = _koin!!
                else -> System.err.println("Handling from other Test classes is not supported for: $testInstance!")
                    .also { TODO("Handling from other Test classes is not supported for: $testInstance!") }
            }
        }
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return (parameterContext.parameter.type == Koin::class.java)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        when (parameterContext.parameter.type) {
            Koin::class.java -> return this._koin!!
            else -> TODO("No other parameters are supported!")
        }
    }
}
