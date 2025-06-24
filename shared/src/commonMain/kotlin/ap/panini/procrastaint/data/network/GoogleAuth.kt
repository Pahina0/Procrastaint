package ap.panini.procrastaint.data.network

import ap.panini.procrastaint.data.repositories.PreferenceRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.AuthConfig
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

internal expect fun getGoogleId(): String

/**
 * Google auth
 *
 * You need to get the initial access/refresh token yourself and create the calendar
 * when approved. this only cycles tokens assuming you have access already
 *
 * @constructor Create empty Google auth
 */
internal object GoogleAuth {
    internal fun AuthConfig.auth(
        preferenceRepository: PreferenceRepository,
    ) {
        bearer {
            loadTokens {
                val access =
                    preferenceRepository.getString(PreferenceRepository.GOOGLE_ACCESS_TOKEN).first()

                if (access.isBlank()) return@loadTokens null

                val refresh =
                    preferenceRepository.getString(PreferenceRepository.GOOGLE_REFRESH_TOKEN)
                        .first()

                if (refresh.isBlank()) return@loadTokens null
                BearerTokens(access, refresh)
            }

            refreshTokens {
                val client = HttpClient {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                isLenient = true
                                ignoreUnknownKeys = true
                            }
                        )
                    }
                }
                val refreshTokenInfo: TokenInfo =
                    client.submitForm(
                        url = "https://accounts.google.com/o/oauth2/token",
                        formParameters = parameters {
                            append("grant_type", "refresh_token")
                            append("client_id", getGoogleId())
                            append("refresh_token", oldTokens?.refreshToken ?: "")
                        }
                    ) { markAsRefreshTokenRequest() }.body()

                val access = refreshTokenInfo.accessToken
                val refresh = oldTokens?.refreshToken!!

                preferenceRepository.setString(PreferenceRepository.GOOGLE_ACCESS_TOKEN, access)
                preferenceRepository.setString(PreferenceRepository.GOOGLE_REFRESH_TOKEN, refresh)
                BearerTokens(access, refresh)
            }

            sendWithoutRequest { request ->
                request.url.host == "www.googleapis.com"
            }
        }
    }
}
