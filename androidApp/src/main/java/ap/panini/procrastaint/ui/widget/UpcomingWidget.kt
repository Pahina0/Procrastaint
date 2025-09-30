package ap.panini.procrastaint.ui.widget

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
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
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import ap.panini.procrastaint.ui.AddTaskActivity
import ap.panini.procrastaint.ui.widget.components.HorizontalDivider
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import java.util.Date

class UpcomingWidget(private val viewModel: UpcomingWidgetViewModel) : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {


        provideContent {
            GlanceTheme {
                GlanceContent(context)
            }
        }
    }

    @Composable
    private fun GlanceContent(context: Context) {
        val tasks by viewModel.upcomingTasks.collectAsState()
        val recentlyCompleted by viewModel.recentlyCompleted.collectAsState()
        val groupedTasks = tasks.groupBy {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.currentEventTime
            cal.get(Calendar.DAY_OF_YEAR)
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .background(GlanceTheme.colors.surface),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {

            Button(
                text = "Add task",
                onClick = actionStartActivity<AddTaskActivity>(),
                modifier = GlanceModifier.fillMaxWidth()
            )


            LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
                groupedTasks.forEach { (_, tasks) ->
                    item {
                        val date = Date(tasks.first().currentEventTime)
                        Text(
                            text = DateFormat.format("EEEE, MMMM d", date).toString(),
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onSurface
                            ),
                            modifier = GlanceModifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        HorizontalDivider(height = 2.dp)
                    }
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
                            Column(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = task.title,
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface,
                                        textDecoration = if (task.completed != null || recentlyCompleted.contains(
                                                task.taskId
                                            )
                                        ) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    modifier = GlanceModifier.defaultWeight()
                                )
                                Text(
                                    text = task.currentEventTime.formatMilliseconds(
                                        setOf(
                                            Time.HOUR,
                                            Time.MINUTE
                                        )
                                    ),
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                )

                            }
                        }
                    }

                    item {
                        HorizontalDivider(height = 1.dp)
                    }
                }
            }
        }
    }
}

class UpcomingWidgetReceiver : GlanceAppWidgetReceiver(), KoinComponent {
    override val glanceAppWidget: GlanceAppWidget by lazy {
        val viewModel: UpcomingWidgetViewModel by inject()
        UpcomingWidget(viewModel)
    }
}

