package ap.panini.procrastaint.ui.agenda.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

sealed interface AgendaDayItem {
    val sortKey: Long
    val isAllDayOrUntimed: Boolean
    val itemKey: String

    data class Event(val event: ExternalCalendarEvent) : AgendaDayItem {
        override val isAllDayOrUntimed: Boolean get() = event.isAllDay
        override val sortKey: Long get() = event.startTime
        override val itemKey: String get() = "event_${event.id}_${event.startTime}"
    }

    data class Task(val task: TaskSingle) : AgendaDayItem {
        override val isAllDayOrUntimed: Boolean get() = task.startTime == null
        override val sortKey: Long get() = task.currentEventTime
        override val itemKey: String get() = "task_${task.taskId}_${task.currentEventTime}"
    }
}

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
    overdueTasks: List<TaskSingle> = emptyList(),
    onMoveAllOverdueToToday: (() -> Unit)? = null,
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

    if (distinctDates.isEmpty() && overdueTasks.isEmpty()) {
        EmptyPage(
            icon = Icons.Outlined.CheckCircleOutline,
            text = if (isFilterActive) "No matching tasks found" else "No upcoming tasks!\nTap + to add one.",
            modifier = modifier.fillMaxSize()
        )
    } else {
        val listState = rememberLazyListState()
        var isOverdueExpanded by remember { mutableStateOf(false) }

        val hasOverdue = overdueTasks.isNotEmpty() && !isFilterActive
        val overdueTasksCount = if (isOverdueExpanded) overdueTasks.size else 0
        val overdueItemCount = if (hasOverdue) 1 + overdueTasksCount else 0

        val dateToIndexMap = remember(
            distinctDates,
            scheduledTasksByDate,
            externalEventsByDate,
            overdueItemCount
        ) {
            val map = mutableMapOf<Long, Int>()
            var currentIndex = overdueItemCount
            for (date in distinctDates) {
                map[date] = currentIndex
                val eventCount = externalEventsByDate[date]?.size ?: 0
                val taskCount = scheduledTasksByDate[date]?.size ?: 0
                val emptyCount = if (eventCount == 0 && taskCount == 0) 1 else 0
                currentIndex += 1 + eventCount + taskCount + emptyCount
            }
            map
        }

        val indexToDateMap = remember(
            distinctDates,
            scheduledTasksByDate,
            externalEventsByDate,
            overdueItemCount,
            today
        ) {
            val map = mutableMapOf<Int, Long>()
            for (i in 0 until overdueItemCount) {
                map[i] = today
            }
            var currentIndex = overdueItemCount
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
                val targetIndex = if (normalizedFocused == today && hasOverdue) {
                    0
                } else {
                    dateToIndexMap[normalizedFocused]
                        ?: distinctDates.firstOrNull { it >= normalizedFocused }?.let { dateToIndexMap[it] }
                        ?: distinctDates.lastOrNull()?.let { dateToIndexMap[it] }
                }
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
                val targetIndex = if (normalizedTarget == today && hasOverdue) {
                    0
                } else {
                    dateToIndexMap[normalizedTarget]
                        ?: distinctDates.firstOrNull { it >= normalizedTarget }?.let { dateToIndexMap[it] }
                }
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
            if (hasOverdue) {
                stickyHeader(key = "header_overdue") {
                    AgendaOverdueHeader(
                        overdueCount = overdueTasks.size,
                        isExpanded = isOverdueExpanded,
                        onToggleExpand = { isOverdueExpanded = !isOverdueExpanded },
                        onMoveAllToToday = { onMoveAllOverdueToToday?.invoke() }
                    )
                }

                if (isOverdueExpanded) {
                    items(
                        items = overdueTasks,
                        key = { "overdue_${it.taskId}_${it.currentEventTime}" }
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

                val dayItems = buildList {
                    eventsForDate.forEach { add(AgendaDayItem.Event(it)) }
                    tasksForDate.forEach { add(AgendaDayItem.Task(it)) }
                }.sortedWith(
                    compareBy<AgendaDayItem> { !it.isAllDayOrUntimed }
                        .thenBy { it.sortKey }
                )

                if (dayItems.isNotEmpty()) {
                    items(
                        items = dayItems,
                        key = { it.itemKey }
                    ) { item ->
                        when (item) {
                            is AgendaDayItem.Event -> ExternalEventView(event = item.event)
                            is AgendaDayItem.Task -> TaskView(
                                task = item.task,
                                onCheck = onCheckTask,
                                onEdit = onEditTask,
                                recentlyCompleted = recentlyCompleted
                            )
                        }
                    }
                } else {
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

@Composable
private fun AgendaOverdueHeader(
    overdueCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onMoveAllToToday: () -> Unit,
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
                .clickable(onClick = onToggleExpand)
                .padding(horizontal = HeaderHorizontalPadding, vertical = HeaderVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Overdue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )

                Badge(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(
                        text = overdueCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = HeaderBadgeHorizontalPadding)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse overdue tasks" else "Expand overdue tasks",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            TextButton(
                onClick = onMoveAllToToday,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = "Move to Today",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
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
