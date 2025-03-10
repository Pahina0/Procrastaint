package ap.panini.procrastaint.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import ap.panini.procrastaint.data.database.ProcrastaintDatabase
import ap.panini.procrastaint.data.network.GoogleAuth
import ap.panini.procrastaint.data.network.api.GoogleCalendarApi
import ap.panini.procrastaint.data.network.api.createGoogleCalendarApi
import ap.panini.procrastaint.data.repositories.CalendarRepository
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.data.repositories.TaskRepository
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.AuthConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import org.koin.dsl.module

val appModule = module {
    val database = getRoomDatabase(getDatabaseBuilder())
    single { database.getTaskDao() }
    single { createDataStore() }
    single { TaskRepository(get(), get(), get()) }
    single { PreferenceRepository(get()) }

    single {
        getKtor(
            GoogleCalendarApi.BASE_URL,
        ) {
            with(GoogleAuth) {
                auth(get())
            }
        }
            .createGoogleCalendarApi()
    }

    single { CalendarRepository(get(), get()) }
}

fun getKtor(
    url: String,
    authConfig: AuthConfig.() -> Unit = {}
): Ktorfit {
    return ktorfit {
        baseUrl(url)
        httpClient(
            HttpClient {
                install(Auth) {
                    authConfig()
                }

                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            println(message)
                        }
                    }
                    level = LogLevel.ALL
                }

                install(DefaultRequest) {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                }

                install(ContentNegotiation) {
                    json(

                        Json {
                            encodeDefaults = true
                            isLenient = true
                            ignoreUnknownKeys = true
                            explicitNulls = false
                        }
                    )
                }
            }
        )
    }
}

fun getRoomDatabase(builder: RoomDatabase.Builder<ProcrastaintDatabase>): ProcrastaintDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<ProcrastaintDatabase>

// https://developer.android.com/kotlin/multiplatform/datastore
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal const val DataStoreFileName = "procrastaint.preferences_pb"

expect fun createDataStore(): DataStore<Preferences>
