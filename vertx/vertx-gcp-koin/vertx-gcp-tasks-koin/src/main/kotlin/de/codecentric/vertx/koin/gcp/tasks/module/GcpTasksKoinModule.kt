package de.codecentric.vertx.koin.gcp.tasks.module

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.cloud.tasks.v2.CloudTasksClient
import com.google.cloud.tasks.v2.CloudTasksSettings
import com.google.cloud.tasks.v2.CreateQueueRequest
import com.google.cloud.tasks.v2.HttpMethod
import com.google.cloud.tasks.v2.HttpRequest
import com.google.cloud.tasks.v2.LocationName
import com.google.cloud.tasks.v2.Queue
import com.google.cloud.tasks.v2.QueueName
import com.google.cloud.tasks.v2.Task
import com.google.protobuf.ByteString
import de.codecentric.koin.core.KoinModule
import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.koin.core.qualifier
import de.codecentric.koin.core.toKoinModuleWithOrder
import de.codecentric.vertx.koin.gcp.core.extension.GcpCoreExtensions.getOrCreate
import de.codecentric.vertx.koin.gcp.core.module.GcpCoreKoinQualifiers.*
import de.codecentric.vertx.koin.gcp.tasks.module.GcpPubSubCommonModule.getOrCreateQueue
import de.codecentric.vertx.koin.gcp.tasks.module.GcpPubSubCommonModule.getOrCreateTask
import de.codecentric.vertx.koin.gcp.tasks.module.GcpTasksKoinQualifiers.*
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

class GcpTasksKoinModule : KoinModule {
    private val gcpTasksOrderedKoinModule = module {
        scope(GCP_TASKS_QUEUE_SCOPE.qualifier) {
            scoped(GCP_TASKS_QUEUE_ID.qualifier) { this.id }

            scoped(GCP_TASKS_QUEUE_LOCATION_ID.qualifier) { "europe-west3" }

            scoped(GCP_TASKS_QUEUE_LOCATION_NAME.qualifier) {
                when (getOrNull<String>(GCP_TASKS_QUEUE_ID.qualifier)) {
                    null -> throw NoBeanDefFoundException("$GCP_TASKS_QUEUE_ID must be defined to get a scoped $GCP_TASKS_QUEUE_LOCATION_NAME")
                    else -> LocationName.format(get(GCP_PROJECT_NAME.qualifier), get(GCP_TASKS_QUEUE_LOCATION_ID.qualifier))
                }
            }

            scoped(GCP_TASKS_QUEUE_NAME.qualifier) {
                when (val queueId = getOrNull<String>(GCP_TASKS_QUEUE_ID.qualifier)) {
                    null -> throw NoBeanDefFoundException("$GCP_TASKS_QUEUE_ID must be defined to get a scoped $GCP_TASKS_QUEUE_NAME")
                    else -> QueueName.format(get(GCP_PROJECT_NAME.qualifier), get(GCP_TASKS_QUEUE_LOCATION_ID.qualifier), queueId)
                }
            }

            scoped(GCP_TASKS_QUEUE.qualifier) {
                getOrCreateQueue(get(GCP_CORE_FIXED_CREDENTIALS_PROVIDER.qualifier), get(GCP_TASKS_QUEUE_LOCATION_NAME.qualifier), get(GCP_TASKS_QUEUE_NAME.qualifier))
            }

            factory(GCP_TASKS_QUEUE_TASK.qualifier) { (gcpTasksQueueTask: GcpTasksQueueTask) ->
                val task: Task = Task.newBuilder()
                    .setHttpRequest(gcpTasksQueueTask.toHttpRequest())
                    .build()

                getOrCreateTask(get(GCP_CORE_FIXED_CREDENTIALS_PROVIDER.qualifier), get(GCP_TASKS_QUEUE_NAME.qualifier), task)
            }
        }
    }.toKoinModuleWithOrder(moduleName = "gcpTasksOrderedKoinModule")

    override val koinOrderedModules: LinkedHashSet<KoinModuleWithOrder> = linkedSetOf(gcpTasksOrderedKoinModule)
}

// TODO create client in scope and close it with scope, not every time the function ends
internal object GcpPubSubCommonModule {
    fun <T> getOrCreateCloudTasksClient(credentialsProvider: FixedCredentialsProvider, executeFn: (CloudTasksClient) -> T): T = CloudTasksClient
        .create(CloudTasksSettings.newBuilder().setCredentialsProvider(credentialsProvider).build())
        .use(executeFn)

    fun getOrCreateQueue(credentialsProvider: FixedCredentialsProvider, locationName: String, queueName: String): Queue {
        getOrCreateCloudTasksClient(credentialsProvider) { cloudTasksClient ->
            return@getOrCreateCloudTasksClient getOrCreate(
                { cloudTasksClient.getQueue(queueName) },
                { cloudTasksClient.createQueue(CreateQueueRequest.newBuilder().setParent(locationName).setQueue(Queue.newBuilder().setName(queueName).build()).build()) }
            )
        }
        throw Exception("No client available!")
    }

    fun getOrCreateTask(credentialsProvider: FixedCredentialsProvider, queueName: String, task: Task) {
        getOrCreateCloudTasksClient(credentialsProvider) { cloudTasksClient ->
            cloudTasksClient.createTask(queueName, task)
        }
    }
}

data class GcpTasksQueueTask(val url: String, val httpMethod: HttpMethod, val body: String) {
    fun toHttpRequest(): HttpRequest = HttpRequest.newBuilder()
        .setUrl(url)
        .setHttpMethod(httpMethod)
        .setBody(bodyToByteString())
        .build()

    private fun bodyToByteString() = ByteString.copyFromUtf8(body)
}

enum class GcpTasksKoinQualifiers(val qualifier: StringQualifier) {
    GCP_TASKS_QUEUE_SCOPE("GCP_TASKS_QUEUE_SCOPE".qualifier()),
    GCP_TASKS_QUEUE_ID("GCP_TASKS_QUEUE_ID".qualifier()),
    GCP_TASKS_QUEUE_NAME("GCP_TASKS_QUEUE_NAME".qualifier()),
    GCP_TASKS_QUEUE_LOCATION_ID("GCP_TASKS_QUEUE_LOCATION_ID".qualifier()),
    GCP_TASKS_QUEUE_LOCATION_NAME("GCP_TASKS_QUEUE_LOCATION_NAME".qualifier()),
    GCP_TASKS_QUEUE("GCP_TASKS_QUEUE".qualifier()),
    GCP_TASKS_QUEUE_TASK("GCP_TASKS_QUEUE_TASK".qualifier()),
    GCP_TASKS("GCP_TASKS".qualifier());
}
