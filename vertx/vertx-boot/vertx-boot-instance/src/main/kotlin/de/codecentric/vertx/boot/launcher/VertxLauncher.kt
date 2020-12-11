@file:OptIn(KoinApiExtension::class)

package de.codecentric.vertx.boot.launcher

import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.kotlin.logger.loggerWithTab
import de.codecentric.util.fnresult.handleThrowable
import de.codecentric.util.fnresult.onFailureEmpty
import de.codecentric.vertx.boot.logger.KoinLogger
import de.codecentric.vertx.common.util.doNothing
import de.codecentric.vertx.koin.core.command.CCBareCommandFactory
import de.codecentric.vertx.koin.core.module.ClusterVertxKoinQualifiers.CLUSTER_VERTX_CLUSTER_MANAGER
import de.codecentric.vertx.koin.core.module.VertxConfigKoinModule
import de.codecentric.vertx.koin.core.module.VertxKoinModule
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future.future
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.impl.launcher.VertxCommandLauncher
import io.vertx.core.impl.launcher.VertxLifecycleHooks
import io.vertx.core.json.JsonObject
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import java.util.concurrent.TimeUnit
import java.util.logging.Level.FINE
import java.util.logging.Level.FINEST
import java.util.logging.Level.INFO
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

interface VertxLauncher : VertxLifecycleHooks, KoinComponent {
    val args: Array<String>
    val mainVerticleClass: String?

    val orderedModules: MutableSet<KoinModuleWithOrder>
    val overrideModules: MutableSet<KoinModuleWithOrder>

    fun run()
    fun stop()
}

abstract class AbstractVertxLauncher(final override val args: Array<String>) : VertxCommandLauncher(), VertxLauncher {
    private val logTab = 10
    private val ccBareCommandFactory = CCBareCommandFactory()

    private val runArgs: MutableList<String> = args.toMutableList()

    private lateinit var vertxDelegate: Vertx
    private var clusterManager: ClusterManager? = null

    final override val orderedModules: MutableSet<KoinModuleWithOrder> = mutableSetOf()
    final override val overrideModules: MutableSet<KoinModuleWithOrder> = mutableSetOf()

    init {
        this.register(ccBareCommandFactory)
    }

    override fun afterConfigParsed(config: JsonObject?) {
        logger.loggerWithTab(logTab - 2, FINEST) { " <- afterConfigParsed config=$config" }
    }

    override fun beforeStartingVertx(vertxOptions: VertxOptions) {
        // TODO CLEANUP remove this after testing is done for blocking
        vertxOptions.blockedThreadCheckInterval = TimeUnit.SECONDS.toMillis(60)

        clusterManager?.let { vertxOptions.setClusterManager(clusterManager) }

        logger.loggerWithTab(logTab - 2, FINEST) { " <- beforeStartingVertx: vertxOptions=$vertxOptions" }
    }

    override fun afterStartingVertx(vertx: Vertx) {
        logger.loggerWithTab(logTab - 2, FINEST) { " <- afterStartingVertx: vertx=${vertx.hashCode()}" }

        vertxDelegate = vertx
        vertxDelegate.exceptionHandler {
            logger.error("Exception happened in vertx: $it", it)
            handleFailureInVertx(vertx, it)
        }

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                vertx.close()
            }
        })
    }

    override fun beforeDeployingVerticle(deploymentOptions: DeploymentOptions) {
        logger.loggerWithTab(logTab - 2, FINEST) { " <- beforeDeployingVerticle: deploymentOptions=${deploymentOptions.toJson()}" }
    }

    override fun beforeStoppingVertx(vertx: Vertx?) {
        logger.loggerWithTab(logTab - 2, FINEST) { " <- beforeStoppingVertx: vertx=$vertx" }
    }

    override fun afterStoppingVertx() {
        logger.loggerWithTab(logTab - 2, FINEST) { " <- afterStoppingVertx" }
    }

    override fun handleDeployFailed(vertx: Vertx?, mainVerticle: String?, deploymentOptions: DeploymentOptions?, cause: Throwable?) {
        logger.error("handleDeployFailed: vertx=$vertx - mainVerticle=$mainVerticle - deploymentOptions=$deploymentOptions - cause:$cause")
        handleFailureInVertx(vertx, cause)
    }

    private fun handleFailureInVertx(vertx: Vertx?, cause: Throwable?) {
        logger.error("Closing because: $cause")

        if (vertx?.isClustered == true) {
            val clusterManager: ClusterManager = get(CLUSTER_VERTX_CLUSTER_MANAGER.qualifier)
            future<Void> { clusterManager.leave(it) }.onComplete { }
        } else {
            doNothing()
        }

        vertx?.close()

        // killProcess()
    }

    override fun run() {
        orderedModules.addAll(VertxConfigKoinModule().koinOrderedModules)

        handleThrowable { runInternal() }
            .onFailureEmpty { handleFailureInVertx(vertxDelegate, it.cause) }
    }

    override fun stop() {
        runBlocking { vertxDelegate.close().await() }

        stopKoin()
    }

    private fun runInternal() {
        logger.loggerWithTab(10, INFO) { " -> Starting to run..." }

        if (hasClusterArg(runArgs)) {
            if (runArgs.isEmpty()) runArgs.add(VERTX_CLI_CLUSTER_DEFAULT_COMMAND)
        } else {
            if (runArgs.isEmpty()) runArgs.add(VERTX_CLI_INSTANCE_DEFAULT_COMMAND)
        }

        logger.loggerWithTab(10, FINE) { " -> Dispatching instance launcher with runArgs: $runArgs..." }
        dispatch(runArgs.toTypedArray())

        logger.trace { "Checking for the vertx instance..." }
        checkVertxInstance()

        if (hasClusterArg(runArgs)) {
            clusterManager = getKoin().get<ClusterManager>(CLUSTER_VERTX_CLUSTER_MANAGER.qualifier)
            logger.info { "Found clusterManager: ${clusterManager!!.nodeInfo} -> ${clusterManager!!.isActive}" }
        }

        logger.loggerWithTab(8, INFO) { " -> Running koin start..." }

        val koinStartTime = measureTimeMillis {
            val toAddModules = mutableListOf<KoinModuleWithOrder>()
                .apply {
                    addAll(orderedModules)
                    addAll(overrideModules)

                    addAll(VertxKoinModule(vertxDelegate).koinOrderedModules)
                }
                .sortedBy { it.order }
                .reversed()
            logger.loggerWithTab(6, FINEST) { " -> orderedModules found: ${orderedModules.size}..." }
            logger.loggerWithTab(6, FINEST) { " -> overrideModules found: ${overrideModules.size}..." }
            logger.loggerWithTab(6, FINEST) { "\nFound definitions and order: ${toAddModules.map { it.toString() }}" }

            startKoin {
                logger(KoinLogger(logger))

                modules(toAddModules.map { it.module }.toList())
            }
        }
        logger.loggerWithTab(8) { " <- Koin app done in: $koinStartTime ms" }

        logger.loggerWithTab(8, INFO) { " -> Deploying main verticle: $mainVerticleClass..." }
        val time2 = measureTimeMillis {
            mainVerticleClass?.let {
                val deploymentId = runBlocking { vertxDelegate.deployVerticle(it).await() }
                logger.loggerWithTab(8, INFO) { " <- MainVerticle $it deployed with id: $deploymentId!" }
            }
        }
        logger.loggerWithTab(8, FINEST) { " <- Done deploying main verticle in: $time2 ms" }

        if (hasClusterArg(runArgs)) logger.loggerWithTab(10, INFO) { " <- Run of type cluster done..." }
        else logger.loggerWithTab(10, INFO) { " <- Run of type instance done..." }
    }

    private fun checkVertxInstance() {
        if (!::vertxDelegate.isInitialized) {
            throw VertxDelegationException("Cannot use run function without initializing Vert.X object!")
        } else {
            val vertxType = if (vertxDelegate.isClustered) "cluster" else "instance"
            logger.loggerWithTab(8, FINEST) { " <- Vert.x[$vertxType]  created: ${vertxDelegate.hashCode()}" }
        }
    }

    companion object {
        const val VERTX_CLI_INSTANCE_DEFAULT_COMMAND: String = "cc-bare"
        private const val CLUSTER_COMMAND_ARG: String = "-cluster"
        const val VERTX_CLI_CLUSTER_DEFAULT_COMMAND: String = "cc-bare $CLUSTER_COMMAND_ARG"

        private fun hasClusterArg(args: Array<String>): Boolean = args.contains(CLUSTER_COMMAND_ARG)

        fun hasClusterArg(args: MutableList<String>): Boolean = args.contains(CLUSTER_COMMAND_ARG)

        fun getClusterArgs(args: Array<String>): Array<String> {
            return if (!hasClusterArg(args)) args.plus(CLUSTER_COMMAND_ARG) else args
        }

        fun getClusterArgs(args: MutableList<String>): MutableList<String> {
            if (!hasClusterArg(args)) args.add(CLUSTER_COMMAND_ARG)
            return args
        }
    }
}

open class DefaultVertxLauncher(args: Array<String>) : AbstractVertxLauncher(args) {
    override var mainVerticleClass: String? = null
}

class VertxDelegationException(override val message: String) : RuntimeException()
