package ap.panini.procrastaint.services

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.ContentResolver
import android.content.Context
import android.content.SyncResult
import android.net.Network
import android.os.Bundle
import ap.panini.procrastaint.data.repositories.NetworkCalendarRepository
import org.koin.java.KoinJavaComponent.inject

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
class SyncAdapter @JvmOverloads constructor(
    context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
    val nsRepo: NetworkCalendarRepository,
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {


    override fun onPerformSync(
        account: Account,
        extras: Bundle,
        authority: String,
        provider: ContentProviderClient,
        syncResult: SyncResult
    ) {
    }
}
