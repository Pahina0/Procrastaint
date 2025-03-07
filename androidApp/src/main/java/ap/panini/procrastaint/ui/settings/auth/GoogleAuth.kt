package ap.panini.procrastaint.ui.settings.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import ap.panini.procrastaint.data.network.getGoogleId
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

class GoogleAuth(context: Context) : OAuth {
    private val authRequest: AuthorizationRequest
    override val authService: AuthorizationService

    init {

        val authorizationServiceConfig = AuthorizationServiceConfiguration(
            "https://accounts.google.com/o/oauth2/auth".toUri(),
            "https://accounts.google.com/o/oauth2/token".toUri()
        )

        AuthorizationServiceConfiguration.fetchFromUrl(
            "https://accounts.google.com/.well-known/openid-configuration".toUri(),
            AuthorizationServiceConfiguration.RetrieveConfigurationCallback { _, ex ->
                if (ex != null) {
                    return@RetrieveConfigurationCallback
                }
                // use serviceConfiguration as needed
            }
        )

        authRequest =
            AuthorizationRequest.Builder(
                authorizationServiceConfig, // the authorization service configuration
                getGoogleId(),
                ResponseTypeValues.CODE, // the response_type value: we want a code
                "ap.panini.procrastaint:/oauth2redirect".toUri()
            ).setScopes("https://www.googleapis.com/auth/calendar.app.created")
                .build()

        authService = AuthorizationService(context)
    }

    override fun auth(
        context: Context,
    ) {
        val authService = AuthorizationService(context)

        authService.performAuthorizationRequest(
            authRequest,
            PendingIntent.getActivity(
                context,
                1,
                Intent(context, AuthActivity::class.java),
                PendingIntent.FLAG_MUTABLE
            ),
            PendingIntent.getActivity(
                context,
                1,
                Intent(context, AuthActivity::class.java),
                PendingIntent.FLAG_MUTABLE
            )
        )
    }
}
