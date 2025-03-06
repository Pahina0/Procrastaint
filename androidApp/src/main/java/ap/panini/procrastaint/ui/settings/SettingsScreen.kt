package ap.panini.procrastaint.ui.settings

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Destination<RootGraph>
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    Scaffold(modifier = modifier) {
        Box(modifier = Modifier.padding(it)) {

            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxSize()
            ) {
                Button(
                    onClick = {
                        // change sha1 on console if releasing
                        val id =
//                            "402611322616-kpkp200hbefrtd7gubstnrqok90ks0f6.apps.googleusercontent.com"
                            "402611322616-02s8oalf6nc9jaqmd1kd8hh28br459kq.apps.googleusercontent.com"
                        val signInWithGoogleOption: GetSignInWithGoogleOption =
                            GetSignInWithGoogleOption
                                .Builder(id)
                                .build()

                        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                            .setServerClientId(id)
                            .setFilterByAuthorizedAccounts(false)
                            .build()

                        val request: GetCredentialRequest =
                            GetCredentialRequest.Builder()
//                                .addCredentialOption(signInWithGoogleOption)
                                .addCredentialOption(googleIdOption)
                                .build()

                        val cm = CredentialManager.create(context)

                        coroutineScope.launch {
                            try {
                                val result = cm.getCredential(
                                    request = request,
                                    context = context,
                                )
                                println(result)

                                if (
                                    handleSignIn(result)
                                ) viewModel.googleLogin()
                            } catch (e: GetCredentialCancellationException) {
                                println(e.type)
                                println(e.stackTraceToString())
                            } catch (e: Exception) {
                                println(e.stackTraceToString())
                            }
                        }


                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign in with Google")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun handleSignIn(result: androidx.credentials.GetCredentialResponse): Boolean {

    if (result.credential
                is CustomCredential &&
        result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        // Use googleIdTokenCredential and extract the ID to validate and
        // authenticate on your server.
        val googleIdTokenCredential = GoogleIdTokenCredential
            .createFrom(result.credential.data)
        println(3)
        println(googleIdTokenCredential)
        return true
    }

    return false
}
