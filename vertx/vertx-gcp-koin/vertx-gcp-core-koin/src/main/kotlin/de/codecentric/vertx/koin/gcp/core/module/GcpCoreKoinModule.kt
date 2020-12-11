package de.codecentric.vertx.koin.gcp.core.module

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import de.codecentric.koin.core.KoinModule
import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.koin.core.qualifier
import de.codecentric.koin.core.toKoinModuleWithOrder
import de.codecentric.vertx.koin.core.logger.loggerWithTab
import de.codecentric.vertx.koin.gcp.core.module.GcpCoreKoinQualifiers.GCP_CORE_FIXED_CREDENTIALS_PROVIDER
import de.codecentric.vertx.koin.gcp.core.module.GcpCoreKoinQualifiers.GCP_CORE_GOOGLE_CREDENTIALS
import de.codecentric.vertx.koin.gcp.core.module.GcpCoreKoinQualifiers.GCP_CREDENTIALS_FILENAME
import de.codecentric.vertx.koin.gcp.core.module.GcpCoreKoinQualifiers.GCP_PROJECT_NAME
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

class GcpCoreKoinModule : KoinModule {
    private val googleCredentialsOrderedKoinModule = module {
        single(GCP_PROJECT_NAME.qualifier, override = false) { "change_me_project_name" }

        single(GCP_CREDENTIALS_FILENAME.qualifier) { "${get<String>(GCP_PROJECT_NAME.qualifier)}.json" }

        single(GCP_CORE_GOOGLE_CREDENTIALS.qualifier, createdAtStart = true) {
            val gcpCredentialsFilename = get<String>(GCP_CREDENTIALS_FILENAME.qualifier)
            logger.loggerWithTab(2) { "Searching for credentials file: $gcpCredentialsFilename" }

            val stream = this::class.java.classLoader.getResourceAsStream(gcpCredentialsFilename)
            stream?.let {
                GoogleCredentials
                    .fromStream(stream)
                    .createScoped(listOf(CLOUD_PLATFORM_SCOPE))
            } ?: GoogleCredentials.getApplicationDefault()
        }

        single(GCP_CORE_FIXED_CREDENTIALS_PROVIDER.qualifier, override = true) {
            FixedCredentialsProvider.create(get<GoogleCredentials>(GCP_CORE_GOOGLE_CREDENTIALS.qualifier))
        }
    }.toKoinModuleWithOrder(moduleName = "googleCredentialsOrderedKoinModule")

    override val koinOrderedModules: LinkedHashSet<KoinModuleWithOrder> = linkedSetOf(googleCredentialsOrderedKoinModule)

    private companion object {
        const val CLOUD_PLATFORM_SCOPE = "https://www.googleapis.com/auth/cloud-platform"
    }
}

enum class GcpCoreKoinQualifiers(val qualifier: StringQualifier) {
    GCP_PROJECT_NAME("GCP_PROJECT_NAME".qualifier()),
    GCP_CREDENTIALS_FILENAME("GCP_CREDENTIALS_FILENAME".qualifier()),
    GCP_CORE_GOOGLE_CREDENTIALS("GCP_CORE_GOOGLE_CREDENTIALS".qualifier()),
    GCP_CORE_FIXED_CREDENTIALS_PROVIDER("GCP_CORE_FIXED_CREDENTIALS_PROVIDER".qualifier());
}
