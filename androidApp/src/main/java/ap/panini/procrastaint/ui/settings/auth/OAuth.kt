package ap.panini.procrastaint.ui.settings.auth

import android.content.Context
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse

interface OAuth {
    val authService: AuthorizationService

    fun auth(context: Context)

    fun preformTokenRequest(
        tokenRequest: TokenRequest,
        onSuccess: (TokenResponse) -> Unit = {},
        onException: (AuthorizationException) -> Unit = {}
    ) =
        authService.performTokenRequest(tokenRequest) { resp, ex ->
            if (ex != null) {
                onException(ex)
            } else if (resp != null) {
                onSuccess(resp)
            }
        }
}
