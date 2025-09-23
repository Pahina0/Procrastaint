package ap.panini.procrastaint.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddButtonWidget(private val viewModel: AddButtonWidgetViewModel) : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {


        provideContent {
            GlanceTheme {
                GlanceContent()
            }
        }
    }

    @Composable
    private fun GlanceContent() {
        val tasks by viewModel.upcomingTasks.collectAsState()
        val recentlyCompleted by viewModel.recentlyCompleted.collectAsState()
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .background(GlanceTheme.colors.surface),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Tasks for next week",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )
            LazyColumn {
                items(tasks) { task ->
                    Row(
                        modifier = GlanceModifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CheckBox(
                            checked = task.completed != null || recentlyCompleted.contains(task.taskId),
                            onCheckedChange = actionRunCallback<ToggleTaskAction>(
                                parameters = actionParametersOf(
                                    taskIdKey to task.taskId,
                                    completedKey to (task.completed != null),
                                    completionId to task.completionId
                                )
                            ),
                            modifier = GlanceModifier.padding(end = 8.dp)
                        )
                        Text(
                            text = task.title,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                textDecoration = if (task.completed != null || recentlyCompleted.contains(task.taskId)) TextDecoration.LineThrough else TextDecoration.None
                            ),
                        )
                    }
                }
            }
        }
    }
}

class AddButtonWidgetReceiver : GlanceAppWidgetReceiver(), KoinComponent {
    override val glanceAppWidget: GlanceAppWidget by lazy {
        val viewModel: AddButtonWidgetViewModel by inject()
        AddButtonWidget(viewModel)
    }
}