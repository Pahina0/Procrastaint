package ap.panini.procrastaint.services

import android.accounts.Account
import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.os.IBinder

class SyncService : Service() {
    override fun onCreate() {
        super.onCreate()
        synchronized(sSyncAdapterLock) {
//            sSyncAdapter = sSyncAdapter ?: SyncAdapter(applicationContext, true)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return sSyncAdapter?.syncAdapterBinder ?: throw IllegalStateException()
    }

    companion object {
        // Storage for an instance of the sync adapter
        private var sSyncAdapter: SyncAdapter? = null

        // Object to use as a thread-safe lock
        private val sSyncAdapterLock = Any()

        private const val AUTHORITY = "ap.panini.procrastaint.datasync.provider"
        private const val ACCOUNT_TYPE = "temp.com"
        private const val ACCOUNT = "placeholderaccount"

        fun sync() {

            val settingsBundle = Bundle().apply {
                putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            }

            ContentResolver.requestSync(createSyncAccount(), AUTHORITY, settingsBundle)
        }

        private fun createSyncAccount(): Account {
            return Account(ACCOUNT, ACCOUNT_TYPE)
        }
    }
}