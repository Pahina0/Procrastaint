package ap.panini.procrastaint.di

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import ap.panini.procrastaint.data.repositories.RepositoryCallback
import ap.panini.procrastaint.ui.widget.UpcomingWidgetReceiver

class WidgetUpdaterCallback(private val context: Context) : RepositoryCallback {

    override fun onDataChanged() {
        val intent = Intent(context, UpcomingWidgetReceiver::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, UpcomingWidgetReceiver::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}
