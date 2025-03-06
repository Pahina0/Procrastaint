package ap.panini.procrastaint.data.network

import ap.panini.procrastaint.data.repositories.PreferenceRepository
import io.ktor.client.plugins.auth.AuthConfig
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import kotlinx.coroutines.flow.first
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod

// returns the google client id
expect fun getClientPrefix(): String

// https://github.com/kalinjul/kotlin-multiplatform-oidc?tab=readme-ov-file
suspend fun getAccessToken() {
    val client =
        OpenIdConnectClient(discoveryUri = "https://accounts.google.com/.well-known/openid-configuration") {
            endpoints {
                tokenEndpoint = "<tokenEndpoint>"
                authorizationEndpoint = "<authorizationEndpoint>"
                userInfoEndpoint = null
                endSessionEndpoint = "<endSessionEndpoint>"
            }

            clientId = "${getClientPrefix()}.apps.googleusercontent.com"
            clientSecret = "<clientSecret>"
            scope = "openid profile"
            codeChallengeMethod = CodeChallengeMethod.S256
            redirectUri = "<redirectUri>"
        }
}

fun AuthConfig.getGoogleAuth(pref: PreferenceRepository) {
    return bearer {
        loadTokens {
            BearerTokens(
                pref.getString(PreferenceRepository.GOOGLE_ACCESS_TOKEN).first(),
                pref.getString(PreferenceRepository.GOOGLE_REFRESH_TOKEN).first()
            )
        }
    }

}
