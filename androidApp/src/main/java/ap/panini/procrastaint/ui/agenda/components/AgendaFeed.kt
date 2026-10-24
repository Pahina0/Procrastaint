package ap.panini.procrastaint.ui.agenda.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
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
private const val EmptyTextAlpha = 0.5f
private val EmptyTextVerticalPadding = 12.dp
private const val DefaultAgendaWindowWeeks = 8
private const val DaysInWeek = 7

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaFeed(
    scheduledTasksByDate: Map<Long, List<TaskSingle>>,
    externalEventsByDate: Map<Long, List<ExternalCalendarEvent>>,
    recentlyCompleted: Set<Pair<Long, Long>>,
    focusedDate: Long,
    onCheckTask: (TaskSingle) -> Unit,
    onEditTask: (TaskSingle) -> Unit,
    modifier: Modifier = Modifier,
    isFilterActive: Boolean = false,
    onFocusedDateChange: ((Long) -> Unit)? = null,
    scrollToTodayEvent: SharedFlow<Long>? = null
) {
    val today = remember { Date.getTodayStart() }
    val distinctDates = remember(
        scheduledTasksByDate,
        externalEventsByDate,
        today,
        focusedDate,
        isFilterActive
    ) {
        computeAgendaDates(
            todayStart = today,
            focusedDate = focusedDate,
            taskDates = scheduledTasksByDate.keys,
            eventDates = externalEventsByDate.keys,
            isFilterActive = isFilterActive
        )
    }

    if (distinctDates.isEmpty()) {
        EmptyPage(
            icon = Icons.Outlined.CheckCircleOutline,
            text = if (isFilterActive) "No matching tasks found" else "No upcoming tasks!\nTap + to add one.",
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
                val emptyCount = if (eventCount == 0 && taskCount == 0) 1 else 0
                currentIndex += 1 + eventCount + taskCount + emptyCount
            }
            map
        }

        val indexToDateMap = remember(distinctDates, scheduledTasksByDate, externalEventsByDate) {
            val map = mutableMapOf<Int, Long>()
            var currentIndex = 0
            for (date in distinctDates) {
                val eventCount = externalEventsByDate[date]?.size ?: 0
                val taskCount = scheduledTasksByDate[date]?.size ?: 0
                val emptyCount = if (eventCount == 0 && taskCount == 0) 1 else 0
                val totalForDate = 1 + eventCount + taskCount + emptyCount
                for (i in 0 until totalForDate) {
                    map[currentIndex + i] = date
                }
                currentIndex += totalForDate
            }
            map
        }

        val isDragged by listState.interactionSource.collectIsDraggedAsState()
        var hasScrolledInitial by remember { mutableStateOf(false) }

        LaunchedEffect(focusedDate) {
            if (!isDragged) {
                val normalizedFocused = getStartOfDay(focusedDate)
                val targetIndex = dateToIndexMap[normalizedFocused]
                    ?: distinctDates.firstOrNull { it >= normalizedFocused }?.let { dateToIndexMap[it] }
                    ?: distinctDates.lastOrNull()?.let { dateToIndexMap[it] }
                if (targetIndex != null) {
                    if (!hasScrolledInitial) {
                        hasScrolledInitial = true
                        listState.scrollToItem(targetIndex)
                    } else {
                        listState.animateScrollToItem(targetIndex)
                    }
                }
            }
        }

        LaunchedEffect(scrollToTodayEvent) {
            scrollToTodayEvent?.collect { targetDate ->
                val normalizedTarget = getStartOfDay(targetDate)
                val targetIndex = dateToIndexMap[normalizedTarget]
                    ?: distinctDates.firstOrNull { it >= normalizedTarget }?.let { dateToIndexMap[it] }
                if (targetIndex != null) {
                    listState.animateScrollToItem(targetIndex)
                }
            }
        }

        LaunchedEffect(isDragged) {
            if (isDragged) {
                snapshotFlow { listState.firstVisibleItemIndex }
                    .distinctUntilChanged()
                    .collect { index ->
                        val date = indexToDateMap[index]
                        if (date != null && date != getStartOfDay(focusedDate)) {
                            onFocusedDateChange?.invoke(date)
                        }
                    }
            }
        }

        LaunchedEffect(listState.isScrollInProgress) {
            if (!listState.isScrollInProgress && !isDragged && hasScrolledInitial) {
                val date = indexToDateMap[listState.firstVisibleItemIndex]
                if (date != null && date != getStartOfDay(focusedDate)) {
                    onFocusedDateChange?.invoke(date)
                }
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

                if (eventsForDate.isEmpty() && tasksForDate.isEmpty()) {
                    item(key = "empty_$date") {
                        Text(
                            text = "No tasks or events",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = EmptyTextAlpha),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = HeaderHorizontalPadding,
                                    vertical = EmptyTextVerticalPadding
                                )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
internal fun computeAgendaDates(
    todayStart: Long,
    focusedDate: Long,
    taskDates: Set<Long>,
    eventDates: Set<Long>,
    isFilterActive: Boolean,
    windowWeeks: Int = DefaultAgendaWindowWeeks,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): List<Long> {
    if (isFilterActive) {
        return (taskDates + eventDates)
            .filter { it > 0L }
            .distinct()
            .sorted()
    }

    val normalizedFocused = getStartOfDay(focusedDate)
    val todayLocalDate = Instant.fromEpochMilliseconds(todayStart).toLocalDateTime(timeZone).date
    val maxTime = maxOf(todayStart, normalizedFocused)
    val maxLocalDate = Instant.fromEpochMilliseconds(maxTime).toLocalDateTime(timeZone).date
    val endLocalDate = maxLocalDate.plus((windowWeeks * DaysInWeek).toLong(), DateTimeUnit.DAY)

    val dates = mutableSetOf<Long>()
    var cur = todayLocalDate
    while (cur <= endLocalDate) {
        dates.add(cur.atStartOfDayIn(timeZone).toEpochMilliseconds())
        cur = cur.plus(1, DateTimeUnit.DAY)
    }

    dates.addAll(taskDates.filter { it >= todayStart })
    dates.addAll(eventDates.filter { it >= todayStart })

    return dates.sorted()
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
