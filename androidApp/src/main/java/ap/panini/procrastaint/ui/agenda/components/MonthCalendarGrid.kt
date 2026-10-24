package ap.panini.procrastaint.ui.agenda.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val DaysInWeek = 7
private const val MaxDotsPerDay = 4
private const val OutsideMonthAlpha = 0.38f
private const val TodayContainerAlpha = 0.55f
private const val TodayBorderAlpha = 0.6f

private val MonthGridPadding = 8.dp
private val MonthHeaderVerticalPadding = 4.dp
private val MonthGridVerticalPadding = 4.dp
private val MonthGridRowSpacing = 2.dp
private val CellHeight = 44.dp
private val DateNumberContainerSize = 28.dp
private val DateNumberCornerRadius = 14.dp
private val DateBorderWidth = 1.dp
private val DateNumberShape = RoundedCornerShape(DateNumberCornerRadius)

private val DotSize = 4.dp
private val DotSpacing = 2.dp
private val DotRowHeight = 6.dp
private val DotRowTopPadding = 2.dp

@OptIn(ExperimentalTime::class)
@Composable
fun MonthCalendarGrid(
    focusedDate: Long,
    monthTasksByDate: Map<Long, List<TaskSingle>>,
    onSelectDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeZone = TimeZone.currentSystemDefault()
    val today = remember { Clock.System.todayIn(timeZone) }
    val focusedLocalDate = remember(focusedDate, timeZone) {
        Instant.fromEpochMilliseconds(focusedDate).toLocalDateTime(timeZone).date
    }
    val firstDayOfMonth = remember(focusedLocalDate.year, focusedLocalDate.month) {
        LocalDate(focusedLocalDate.year, focusedLocalDate.month, 1)
    }
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.isoDayNumber % DaysInWeek
    val gridStartDate = remember(firstDayOfMonth, startDayOfWeek) {
        firstDayOfMonth.minus(startDayOfWeek.toLong(), DateTimeUnit.DAY)
    }
    val nextMonth = remember(firstDayOfMonth) {
        firstDayOfMonth.plus(1, DateTimeUnit.MONTH)
    }
    val lastDayOfMonth = remember(nextMonth) {
        nextMonth.minus(1, DateTimeUnit.DAY)
    }
    val totalDaysInGrid = remember(gridStartDate, lastDayOfMonth) {
        val days = gridStartDate.daysUntil(lastDayOfMonth) + 1
        val weeks = (days + DaysInWeek - 1) / DaysInWeek
        weeks * DaysInWeek
    }
    val totalWeeks = totalDaysInGrid / DaysInWeek

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MonthGridPadding, vertical = MonthGridVerticalPadding)
    ) {
        // Weekday header initials row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MonthHeaderVerticalPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dayInitials = listOf("S", "M", "T", "W", "T", "F", "S")
            for (initial in dayInitials) {
                Text(
                    text = initial,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calendar grid rows
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MonthGridRowSpacing)
        ) {
            for (weekIndex in 0 until totalWeeks) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (dayIndex in 0 until DaysInWeek) {
                        val cellIndex = weekIndex * DaysInWeek + dayIndex
                        val cellDate = remember(gridStartDate, cellIndex) {
                            gridStartDate.plus(cellIndex.toLong(), DateTimeUnit.DAY)
                        }
                        val isInCurrentMonth = cellDate.month == focusedLocalDate.month
                        val isToday = cellDate == today
                        val isFocused = cellDate == focusedLocalDate
                        val cellStartOfDay = remember(cellDate, timeZone) {
                            LocalDateTime(
                                year = cellDate.year,
                                month = cellDate.month,
                                day = cellDate.day,
                                hour = 0,
                                minute = 0,
                                second = 0,
                                nanosecond = 0
                            ).toInstant(timeZone).toEpochMilliseconds()
                        }
                        val tasksForDay = monthTasksByDate[cellStartOfDay] ?: emptyList()

                        MonthDayCell(
                            date = cellDate,
                            isInCurrentMonth = isInCurrentMonth,
                            isToday = isToday,
                            isFocused = isFocused,
                            tasks = tasksForDay,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectDate(cellStartOfDay) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    date: LocalDate,
    isInCurrentMonth: Boolean,
    isToday: Boolean,
    isFocused: Boolean,
    tasks: List<TaskSingle>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = when {
        isFocused -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = TodayContainerAlpha)
        else -> Color.Transparent
    }
    val textColor = when {
        isFocused -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        isInCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = OutsideMonthAlpha)
    }
    val borderModifier = if (isToday && !isFocused) {
        Modifier.border(
            BorderStroke(DateBorderWidth, MaterialTheme.colorScheme.primary.copy(alpha = TodayBorderAlpha)),
            DateNumberShape
        )
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .height(CellHeight)
            .clip(DateNumberShape)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "${date.month.name} ${date.day}, ${date.year}"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(DateNumberContainerSize)
                .clip(DateNumberShape)
                .background(containerColor)
                .then(borderModifier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (isFocused || isToday) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (tasks.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = DotRowTopPadding)
                    .height(DotRowHeight),
                horizontalArrangement = Arrangement.spacedBy(DotSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tasks.take(MaxDotsPerDay).forEach { task ->
                    val dotColor = if (task.completed != null) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        task.tags.firstOrNull()?.toRgbOrNull()
                            ?.let { Color(it.first, it.second, it.third) }
                            ?: MaterialTheme.colorScheme.primary
                    }
                    Box(
                        modifier = Modifier
                            .size(DotSize)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(DotRowHeight + DotRowTopPadding))
        }
    }
}
