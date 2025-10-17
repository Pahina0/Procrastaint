package ap.panini.procrastaint.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import ap.panini.procrastaint.di.WidgetUpdaterCallback
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

val taskIdKey = ActionParameters.Key<Long>("taskId")

class ToggleTaskAction : ActionCallback, KoinComponent {
    private val viewModel: UpcomingWidgetViewModel by inject()
    private val widgetUpdater: WidgetUpdaterCallback by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[taskIdKey] ?: return

        viewModel.toggleTaskCompletion(taskId)

        widgetUpdater.onDataChanged()
    }
}
