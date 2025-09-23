package ap.panini.procrastaint.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.di.WidgetUpdaterCallback
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

val taskIdKey = ActionParameters.Key<Long>("taskId")
val completedKey = ActionParameters.Key<Boolean>("completed")
val currentEventTimeKey = ActionParameters.Key<Long>("currentEventTime")
val metaId = ActionParameters.Key<Long>("metaId")
val completionId = ActionParameters.Key<Long>("completionId")

class ToggleTaskAction : ActionCallback, KoinComponent {
    private val viewModel: AddButtonWidgetViewModel by inject()
    private val widgetUpdater: WidgetUpdaterCallback by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[taskIdKey] ?: return
        val completed = parameters[completedKey] ?: return
        val completionId = parameters[completionId] ?: return

        viewModel.toggleTaskCompletion(taskId, completed, completionId)

        widgetUpdater.onDataChanged()
    }

}