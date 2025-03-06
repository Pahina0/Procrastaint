package ap.panini.procrastaint.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import ap.panini.procrastaint.data.database.ProcrastaintDatabase
import ap.panini.procrastaint.data.network.api.GoogleCalendarApi
import ap.panini.procrastaint.data.network.createGoogleCalendarApi
import ap.panini.procrastaint.data.network.getGoogleAuth
import ap.panini.procrastaint.data.repositories.CalendarRepository
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.data.repositories.TaskRepository
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.AuthConfig
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import org.koin.dsl.module

val appModule = module {
    val database = getRoomDatabase(getDatabaseBuilder())
    single { database.getTaskDao() }
    single { createDataStore() }
    single { TaskRepository(get()) }
    single { PreferenceRepository(get()) }
    single { CalendarRepository(get()) }

    single {
        getKtor(GoogleCalendarApi.BASE_URL) {
            getGoogleAuth(get())
        }.createGoogleCalendarApi()
    }
    single { CalendarRepository(get()) }
}


fun getKtor(url: String, authConfig: AuthConfig.() -> Unit): Ktorfit {
    return ktorfit {
        baseUrl(url)
        httpClient(
            HttpClient {
                install(Auth) {
                    authConfig()
                }

                install(ContentNegotiation) {
                    json(
                        Json {
                            isLenient = true
                            ignoreUnknownKeys = true
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
