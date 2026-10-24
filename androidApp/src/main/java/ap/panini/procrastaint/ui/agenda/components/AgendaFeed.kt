package ap.panini.procrastaint.ui.agenda.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.ui.agenda.getStartOfDay
import ap.panini.procrastaint.ui.calendar.components.ExternalEventView
import ap.panini.procrastaint.ui.components.EmptyPage
import ap.panini.procrastaint.ui.components.TaskView
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val FeedContentBottomPadding = 96.dp
private const val HeaderSurfaceAlpha = 0.95f
private val HeaderTonalElevation = 2.dp
private val HeaderHorizontalPadding = 16.dp
private val HeaderVerticalPadding = 10.dp
private val HeaderBadgeHorizontalPadding = 4.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaFeed(
    scheduledTasksByDate: Map<Long, List<TaskSingle>>,
    externalEventsByDate: Map<Long, List<ExternalCalendarEvent>>,
    recentlyCompleted: Set<Pair<Long, Long>>,
    focusedDate: Long,
    onCheckTask: (TaskSingle) -> Unit,
    onEditTask: (TaskSingle) -> Unit,
    modifier: Modifier = Modifier
) {
    val distinctDates = remember(scheduledTasksByDate, externalEventsByDate) {
        (scheduledTasksByDate.keys + externalEventsByDate.keys)
            .filter { date ->
                (scheduledTasksByDate[date]?.isNotEmpty() == true) ||
                    (externalEventsByDate[date]?.isNotEmpty() == true)
            }
            .distinct()
            .sorted()
    }

    if (distinctDates.isEmpty()) {
        EmptyPage(
            icon = Icons.Outlined.CheckCircleOutline,
            text = "No upcoming tasks!\nTap + to add one.",
            modifier = modifier.fillMaxSize()
        )
    } else {
        val listState = rememberLazyListState()

        val dateToIndexMap = remember(distinctDates, scheduledTasksByDate, externalEventsByDate) {
            val map = mutableMapOf<Long, Int>()
            var currentIndex = 0
            for (date in distinctDates) {
                map[date] = currentIndex
                val eventCount = externalEventsByDate[date]?.size ?: 0
                val taskCount = scheduledTasksByDate[date]?.size ?: 0
                currentIndex += 1 + eventCount + taskCount
            }
            map
        }

        LaunchedEffect(focusedDate) {
            val normalizedFocused = getStartOfDay(focusedDate)
            val targetIndex = dateToIndexMap[normalizedFocused]
                ?: distinctDates.firstOrNull { it >= normalizedFocused }?.let { dateToIndexMap[it] }
                ?: distinctDates.lastOrNull()?.let { dateToIndexMap[it] }
            if (targetIndex != null) {
                listState.animateScrollToItem(targetIndex)
            }
        }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = FeedContentBottomPadding)
        ) {
            for (date in distinctDates) {
                val tasksForDate = scheduledTasksByDate[date].orEmpty()
                val eventsForDate = externalEventsByDate[date].orEmpty()
                val remainingCount = tasksForDate.count { task ->
                    task.completed == null && Pair(task.taskId, task.currentEventTime) !in recentlyCompleted
                }
                val isToday = isTodayDate(date)

                stickyHeader(key = "header_$date") {
                    AgendaDateHeader(
                        date = date,
                        isToday = isToday,
                        remainingCount = remainingCount
                    )
                }

                if (eventsForDate.isNotEmpty()) {
                    items(
                        items = eventsForDate,
                        key = { "event_${it.id}_${it.startTime}" }
                    ) { event ->
                        ExternalEventView(event = event)
                    }
                }

                if (tasksForDate.isNotEmpty()) {
                    items(
                        items = tasksForDate,
                        key = { "task_${it.taskId}_${it.currentEventTime}" }
                    ) { task ->
                        TaskView(
                            task = task,
                            onCheck = onCheckTask,
                            onEdit = onEditTask,
                            recentlyCompleted = recentlyCompleted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgendaDateHeader(
    date: Long,
    isToday: Boolean,
    remainingCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = HeaderSurfaceAlpha),
        tonalElevation = HeaderTonalElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HeaderHorizontalPadding, vertical = HeaderVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatAgendaHeaderDate(date),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isToday) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            if (remainingCount > 0) {
                Badge(
                    containerColor = if (isToday) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    contentColor = if (isToday) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                ) {
                    Text(
                        text = remainingCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = HeaderBadgeHorizontalPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
internal fun formatAgendaHeaderDate(
    dateMillis: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    locale: Locale = Locale.getDefault(),
    clock: Clock = Clock.System
): String {
    if (dateMillis <= 0L) return "Today"
    val date = Instant.fromEpochMilliseconds(dateMillis).toLocalDateTime(timeZone).date
    val today = clock.now().toLocalDateTime(timeZone).date
    val tomorrow = today.plus(1, DateTimeUnit.DAY)

    if (date == today) return "Today"
    if (date == tomorrow) return "Tomorrow"

    val pattern = if (date.year != today.year) "EEEE, MMM d, yyyy" else "EEEE, MMM d"
    val formatter = SimpleDateFormat(pattern, locale).apply {
        this.timeZone = java.util.TimeZone.getTimeZone(timeZone.id)
    }
    return formatter.format(java.util.Date(dateMillis))
}

@OptIn(ExperimentalTime::class)
internal fun isTodayDate(
    dateMillis: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    clock: Clock = Clock.System
): Boolean {
    if (dateMillis <= 0L) return true
    val date = Instant.fromEpochMilliseconds(dateMillis).toLocalDateTime(timeZone).date
    val today = clock.now().toLocalDateTime(timeZone).date
    return date == today
}
