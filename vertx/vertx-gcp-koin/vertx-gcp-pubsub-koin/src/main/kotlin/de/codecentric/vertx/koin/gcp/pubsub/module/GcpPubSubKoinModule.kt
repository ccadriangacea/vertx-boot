package de.codecentric.vertx.koin.gcp.pubsub.module

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.Subscriber
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.google.protobuf.ByteString
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.ProjectTopicName
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.PushConfig
import com.google.pubsub.v1.Subscription
import com.google.pubsub.v1.Topic
import de.codecentric.koin.core.KoinModule
import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.koin.core.qualifier
import de.codecentric.koin.core.toKoinModuleWithOrder
import de.codecentric.vertx.koin.gcp.core.extension.GcpCoreExtensions.getOrCreate
import de.codecentric.vertx.koin.gcp.core.module.GcpCoreKoinQualifiers.GCP_CORE_FIXED_CREDENTIALS_PROVIDER
import de.codecentric.vertx.koin.gcp.core.module.GcpCoreKoinQualifiers.GCP_PROJECT_NAME
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubCommonModule.getGcpPubSubMessage
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubCommonModule.getGcpPubSubMessageFromRoutingContext
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubCommonModule.getOrCreateSubscription
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubCommonModule.getOrCreateTopic
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubCommonModule.getPublisher
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubCommonModule.getSubscriber
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubKoinQualifiers.GCP_PUB_SUB_CUSTOM_SUBSCRIPTION
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubKoinQualifiers.GCP_PUB_SUB_MESSAGE
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubKoinQualifiers.GCP_PUB_SUB_MESSAGE_FROM_ROUTING_CONTEXT
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubKoinQualifiers.GCP_PUB_SUB_PUBLISHER
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubKoinQualifiers.GCP_PUB_SUB_SUBSCRIBER
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubKoinQualifiers.GCP_PUB_SUB_SUBSCRIPTION
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubKoinQualifiers.GCP_PUB_SUB_SUBSCRIPTION_ID
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubKoinQualifiers.GCP_PUB_SUB_SUBSCRIPTION_NAME
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubKoinQualifiers.GCP_PUB_SUB_TOPIC
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubKoinQualifiers.GCP_PUB_SUB_TOPIC_ID
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubKoinQualifiers.GCP_PUB_SUB_TOPIC_NAME
import de.codecentric.vertx.koin.gcp.pubsub.module.GcpPubSubKoinQualifiers.GCP_PUB_SUB_TOPIC_SCOPE
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

class GcpPubSubKoinModule : KoinModule {
    private val gcpPubSubOrderedKoinModule = module {
        scope(GCP_PUB_SUB_TOPIC_SCOPE.qualifier) {
            /*
            to override the topicId use: pubSubTopicScope.declareWithOverride(topicId.plus("-test2"), GCP_PUB_SUB_TOPIC_ID.qualifier)
             */
            scoped(GCP_PUB_SUB_TOPIC_ID.qualifier) { this.id }

            scoped(GCP_PUB_SUB_TOPIC_NAME.qualifier) {
                when (val topicId = getOrNull<String>(GCP_PUB_SUB_TOPIC_ID.qualifier)) {
                    null -> throw NoBeanDefFoundException("$GCP_PUB_SUB_TOPIC_ID must be defined to get a scoped $GCP_PUB_SUB_TOPIC_NAME")
                    else -> ProjectTopicName.format(get<String>(GCP_PROJECT_NAME.qualifier), topicId)
                }
            }

            scoped(GCP_PUB_SUB_SUBSCRIPTION_ID.qualifier) { this.id.plus("-all") }

            scoped(GCP_PUB_SUB_SUBSCRIPTION_NAME.qualifier) {
                when (val subscriptionId = getOrNull<String>(GCP_PUB_SUB_SUBSCRIPTION_ID.qualifier)) {
                    null -> throw NoBeanDefFoundException("$GCP_PUB_SUB_SUBSCRIPTION_ID must be defined to get a scoped $GCP_PUB_SUB_SUBSCRIPTION_NAME")
                    else -> ProjectSubscriptionName.format(get(GCP_PROJECT_NAME.qualifier), subscriptionId)
                }
            }

            scoped(GCP_PUB_SUB_TOPIC.qualifier) {
                val credentialsProvider = get<FixedCredentialsProvider>(GCP_CORE_FIXED_CREDENTIALS_PROVIDER.qualifier)
                val topicName = get<String>(GCP_PUB_SUB_TOPIC_NAME.qualifier)

                getOrCreateTopic(credentialsProvider, topicName)
            }

            scoped(GCP_PUB_SUB_SUBSCRIPTION.qualifier) {
                val credentialsProvider = get<FixedCredentialsProvider>(GCP_CORE_FIXED_CREDENTIALS_PROVIDER.qualifier)
                val topicName = get<String>(GCP_PUB_SUB_TOPIC_NAME.qualifier)
                val subscriptionName = get<String>(GCP_PUB_SUB_SUBSCRIPTION_NAME.qualifier)

                getOrCreateSubscription(credentialsProvider, topicName, subscriptionName)
            }

            scoped(GCP_PUB_SUB_PUBLISHER.qualifier) {
                val topic = get<Topic>(GCP_PUB_SUB_TOPIC.qualifier)

                getPublisher(get(GCP_CORE_FIXED_CREDENTIALS_PROVIDER.qualifier), topic.name)
            }

            scoped(GCP_PUB_SUB_SUBSCRIBER.qualifier) { (messageReceiver: MessageReceiver) ->
                val subscription = get<Subscription>(GCP_PUB_SUB_SUBSCRIPTION.qualifier)

                getSubscriber(get(GCP_CORE_FIXED_CREDENTIALS_PROVIDER.qualifier), subscription.name, messageReceiver)
            }

            factory(GCP_PUB_SUB_CUSTOM_SUBSCRIPTION.qualifier) { (subscriptionId: String, messageReceiver: MessageReceiver) ->
                val credentialsProvider = get<FixedCredentialsProvider>(GCP_CORE_FIXED_CREDENTIALS_PROVIDER.qualifier)

                val subscriptionName = ProjectSubscriptionName.format(get<String>(GCP_PROJECT_NAME.qualifier), subscriptionId)
                getOrCreateSubscription(credentialsProvider, get(GCP_PUB_SUB_TOPIC_NAME.qualifier), subscriptionName)

                getSubscriber(credentialsProvider, subscriptionName, messageReceiver)
            }

            factory(GCP_PUB_SUB_MESSAGE.qualifier) { (data: String) ->
                getGcpPubSubMessage(data)
            }

            factory(GCP_PUB_SUB_MESSAGE_FROM_ROUTING_CONTEXT.qualifier) { (routingContext: RoutingContext) ->
                getGcpPubSubMessageFromRoutingContext(routingContext)
            }
        }
    }.toKoinModuleWithOrder(moduleName = "gcpPubSubOrderedKoinModule")

    override val koinOrderedModules: LinkedHashSet<KoinModuleWithOrder> = linkedSetOf(gcpPubSubOrderedKoinModule)
}

internal object GcpPubSubCommonModule {
    fun getOrCreateTopic(credentialsProvider: FixedCredentialsProvider, topicName: String): Topic {
        TopicAdminClient
            .create(TopicAdminSettings.newBuilder().setCredentialsProvider(credentialsProvider).build())
            .use { topicAdminClient ->
                return getOrCreate(
                    { topicAdminClient.getTopic(topicName) },
                    { topicAdminClient.createTopic(topicName) }
                )
            }
    }

    fun getOrCreateSubscription(credentialsProvider: FixedCredentialsProvider, topicName: String, subscriptionName: String): Subscription {
        SubscriptionAdminClient
            .create(SubscriptionAdminSettings.newBuilder().setCredentialsProvider(credentialsProvider).build())
            .use { subscriptionAdminClient ->
                return getOrCreate(
                    { subscriptionAdminClient.getSubscription(subscriptionName) },
                    { subscriptionAdminClient.createSubscription(subscriptionName, topicName, PushConfig.getDefaultInstance(), 10) }
                )
            }
    }

    fun getPublisher(credentialsProvider: FixedCredentialsProvider, topicName: String): Publisher =
        Publisher
            .newBuilder(topicName)
            .setCredentialsProvider(credentialsProvider)
            .build()

    fun getSubscriber(credentialsProvider: FixedCredentialsProvider, subscriptionName: String, messageReceiver: MessageReceiver): Subscriber =
        Subscriber
            .newBuilder(subscriptionName, messageReceiver)
            .setCredentialsProvider(credentialsProvider)
            .build()

    fun getGcpPubSubMessage(data: String): PubsubMessage = PubsubMessage.newBuilder()
        .setData(ByteString.copyFromUtf8(data)).build()

    fun getGcpPubSubMessageFromRoutingContext(routingContext: RoutingContext): PubsubMessage {
        val messageBuilder = PubsubMessage.newBuilder()

        @Suppress("DEPRECATION") val json = JsonObject()
            .put("request-path", routingContext.normalisedPath())
            .put("body", routingContext.bodyAsJson)

        messageBuilder.data = ByteString.copyFrom(json.toBuffer().bytes)

        return messageBuilder.build()
    }
}

enum class GcpPubSubKoinQualifiers(val qualifier: StringQualifier) {
    GCP_PUB_SUB_TOPIC_SCOPE("GCP_PUB_SUB_TOPIC_SCOPE".qualifier()),
    GCP_PUB_SUB_TOPIC("GCP_PUB_SUB_TOPIC".qualifier()),
    GCP_PUB_SUB_TOPIC_ID("GCP_PUB_SUB_TOPIC_ID".qualifier()),
    GCP_PUB_SUB_TOPIC_NAME("GCP_PUB_SUB_TOPIC_NAME".qualifier()),
    GCP_PUB_SUB_SUBSCRIPTION("GCP_PUB_SUB_SUBSCRIPTION".qualifier()),
    GCP_PUB_SUB_SUBSCRIPTION_ID("GCP_PUB_SUB_SUBSCRIPTION_ID".qualifier()),
    GCP_PUB_SUB_SUBSCRIPTION_NAME("GCP_PUB_SUB_SUBSCRIPTION_NAME".qualifier()),
    GCP_PUB_SUB_PUBLISHER("GCP_PUB_SUB_PUBLISHER".qualifier()),
    GCP_PUB_SUB_SUBSCRIBER("GCP_PUB_SUB_SUBSCRIBER".qualifier()),
    GCP_PUB_SUB_CUSTOM_SUBSCRIPTION("GCP_PUB_SUB_CUSTOM_SUBSCRIPTION".qualifier()),
    GCP_PUB_SUB_MESSAGE("GCP_PUB_SUB_MESSAGE".qualifier()),
    GCP_PUB_SUB_MESSAGE_FROM_ROUTING_CONTEXT("GCP_PUB_SUB_MESSAGE_FROM_ROUTING_CONTEXT".qualifier());
}
