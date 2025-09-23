package ap.panini.procrastaint.di

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import ap.panini.procrastaint.ui.widget.AddButtonWidgetReceiver

class WidgetUpdater(private val context: Context) {

    fun updateWidget() {
        val intent = Intent(context, AddButtonWidgetReceiver::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, AddButtonWidgetReceiver::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}
