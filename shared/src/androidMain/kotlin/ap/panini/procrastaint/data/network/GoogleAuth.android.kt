package ap.panini.procrastaint.data.network

import android.content.Context
import ap.panini.procrastaint.R
import org.koin.mp.KoinPlatform.getKoin

actual fun getGoogleId(): String {
    val context: Context = getKoin().get()

    return context.getString(R.string.android_client_id)
}
