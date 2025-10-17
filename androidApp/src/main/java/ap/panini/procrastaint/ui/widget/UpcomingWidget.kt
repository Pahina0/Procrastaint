package ap.panini.procrastaint.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
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
import androidx.glance.unit.ColorProvider
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.ui.AddTaskActivity
import ap.panini.procrastaint.ui.widget.components.HorizontalDivider
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import java.util.Date

val taskSingleKey = ActionParameters.Key<String>("taskSingle")

class UpcomingWidget(private val viewModel: UpcomingWidgetViewModel) : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {


        provideContent {
            GlanceTheme {
                GlanceContent()
            }
        }
    }

    @Composable
    private fun TaskList(groupedTasks: Map<Int, List<TaskSingle>>, recentlyCompleted: Set<Pair<Long, Long>>) {
        LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
            groupedTasks.forEach { (_, tasks) ->
                item {
                    DateHeader(date = Date(tasks.first().currentEventTime))
                }

                item {
                    HorizontalDivider(height = 2.dp)
                }
                items(tasks) { task ->
                    TaskItem(task, recentlyCompleted)
                }

                item {
                    HorizontalDivider(height = 1.dp)
                }
            }
        }
    }

    @Composable
    private fun DateHeader(date: Date) {
        Text(
            text = DateFormat.format("EEEE, MMM d", date).toString(),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface
            ),
            modifier = GlanceModifier.padding(vertical = 8.dp)
        )
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun TaskItem(task: TaskSingle, recentlyCompleted: Set<Pair<Long, Long>>) {
        val isCompleted = task.completed != null || recentlyCompleted.contains(Pair(task.taskId, task.currentEventTime))
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp).clickable(
                actionStartActivity<AddTaskActivity>(
                    parameters = actionParametersOf(
                        taskSingleKey to Json.encodeToString(task)
                    )
                )
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CheckBox(
                checked = isCompleted,
                onCheckedChange = actionRunCallback<ToggleTaskAction>(
                    parameters = actionParametersOf(
                        taskIdKey to task.taskId,
                        completedKey to (task.completed != null),
                        completionId to task.completionId
                    )
                ),
                modifier = GlanceModifier.padding(end = 8.dp)
            )
            Column(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = task.title,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
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

                if (task.tags.isNotEmpty()) {
                    Row(modifier = GlanceModifier.padding(top = 2.dp)) {
                        task.tags.forEach { tag ->
                            Row(modifier = GlanceModifier.padding(end = 4.dp)) {
                                Text(
                                    text = "#",
                                    style = TextStyle(
                                        color = tag.toRgb()
                                            .let {
                                                ColorProvider(Color(it.first, it.second, it.third))
                                            },
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = tag.displayTitle,
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun NoTasksView() {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = ";-;",
                style = TextStyle(
                    fontSize = 48.sp,
                    color = GlanceTheme.colors.onSurface
                )
            )
            Text(
                text = "no upcoming tasks",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface
                )
            )
        }
    }

    @Composable
    private fun GlanceContent() {
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


            if (tasks.isEmpty()) {
                NoTasksView()
            } else {
                TaskList(groupedTasks, recentlyCompleted)
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

