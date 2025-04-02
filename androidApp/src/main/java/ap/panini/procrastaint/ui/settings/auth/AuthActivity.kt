package ap.panini.procrastaint.ui.settings.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import ap.panini.procrastaint.data.repositories.calendars.GoogleCalendarRepository
import ap.panini.procrastaint.ui.MainActivity
import ap.panini.procrastaint.ui.theme.ProcrastaintTheme
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationResponse
import org.koin.android.ext.android.get

class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resp = AuthorizationResponse.fromIntent(intent)

        if (resp == null) {
            returnToSettings()
            return
        }

        setContent {
            ProcrastaintTheme {
                Scaffold {
                    Box(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxSize()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }

        val googleCalendarRepository: GoogleCalendarRepository = get()
        val googleAuth: GoogleAuth = get()
        googleAuth.preformTokenRequest(
            resp.createTokenExchangeRequest(),
            onSuccess = {
                lifecycleScope.launch {
                    googleCalendarRepository.login(it.accessToken, it.refreshToken)
                    returnToSettings()
                }
            },
            onException = {
                returnToSettings()
            }
        )
    }

    private fun returnToSettings() {
        finish()

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }
}
