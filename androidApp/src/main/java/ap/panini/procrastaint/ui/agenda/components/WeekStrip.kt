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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val DaysInWeek = 7
private const val WindowWeeksBefore = 52
private const val WindowWeeksAfter = 52
private const val TotalWindowWeeks = WindowWeeksBefore + WindowWeeksAfter + 1

private const val FocusedWeekdayAlpha = 0.85f
private const val TodayContainerAlpha = 0.55f
private const val TodayBorderAlpha = 0.6f

private val WeekStripHorizontalPadding = 8.dp
private val WeekStripVerticalPadding = 4.dp
private val DayPillSpacing = 2.dp
private val DayPillHeight = 60.dp
private val DayPillCornerRadius = 16.dp
private val DayPillBorderWidth = 1.dp
private val DayPillInnerVerticalPadding = 6.dp
private val DayPillSpacingBetweenText = 2.dp
private val PillShape = RoundedCornerShape(DayPillCornerRadius)

@OptIn(ExperimentalTime::class)
@Composable
fun WeekStrip(
    focusedDate: Long,
    onSelectDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeZone = TimeZone.currentSystemDefault()
    val today = remember { Clock.System.todayIn(timeZone) }
    val currentDayOfWeekOffset = today.dayOfWeek.isoDayNumber % DaysInWeek
    val currentWeekStartDate = remember(today, currentDayOfWeekOffset) {
        today.minus(currentDayOfWeekOffset.toLong(), DateTimeUnit.DAY)
    }

    val focusedLocalDate = remember(focusedDate, timeZone) {
        Instant.fromEpochMilliseconds(focusedDate).toLocalDateTime(timeZone).date
    }
    val focusedDayOffset = focusedLocalDate.dayOfWeek.isoDayNumber % DaysInWeek
    val focusedWeekStartDate = focusedLocalDate.minus(focusedDayOffset.toLong(), DateTimeUnit.DAY)
    val daysDiff = currentWeekStartDate.daysUntil(focusedWeekStartDate)
    val targetPage = (WindowWeeksBefore + (daysDiff / DaysInWeek)).coerceIn(0, TotalWindowWeeks - 1)

    val pagerState = rememberPagerState(
        initialPage = targetPage,
        pageCount = { TotalWindowWeeks }
    )

    LaunchedEffect(targetPage) {
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        val weekOffset = pagerState.settledPage - WindowWeeksBefore
        val pageWeekStartDate = currentWeekStartDate.plus((weekOffset * DaysInWeek).toLong(), DateTimeUnit.DAY)
        val pageWeekEndDate = pageWeekStartDate.plus(DaysInWeek.toLong(), DateTimeUnit.DAY)
        if (focusedLocalDate < pageWeekStartDate || focusedLocalDate >= pageWeekEndDate) {
            val newDate = pageWeekStartDate.plus(focusedDayOffset.toLong(), DateTimeUnit.DAY)
            onSelectDate(newDate.atStartOfDayIn(timeZone).toEpochMilliseconds())
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        key = { pageIndex ->
            val weekOffset = pageIndex - WindowWeeksBefore
            currentWeekStartDate.plus((weekOffset * DaysInWeek).toLong(), DateTimeUnit.DAY).toEpochDays()
        }
    ) { pageIndex ->
        val weekOffset = pageIndex - WindowWeeksBefore
        val pageWeekStartDate = remember(pageIndex, currentWeekStartDate, weekOffset) {
            currentWeekStartDate.plus((weekOffset * DaysInWeek).toLong(), DateTimeUnit.DAY)
        }
        val weekDates = remember(pageWeekStartDate) {
            (0 until DaysInWeek).map { dayIndex ->
                pageWeekStartDate.plus(dayIndex.toLong(), DateTimeUnit.DAY)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WeekStripHorizontalPadding, vertical = WeekStripVerticalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            weekDates.forEach { date ->
                val isFocused = date == focusedLocalDate
                val isToday = date == today
                val dayTimestamp = remember(date, timeZone) {
                    date.atStartOfDayIn(timeZone).toEpochMilliseconds()
                }

                DayPill(
                    date = date,
                    isFocused = isFocused,
                    isToday = isToday,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectDate(dayTimestamp) }
                )
            }
        }
    }
}

@Composable
private fun DayPill(
    date: LocalDate,
    isFocused: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = when {
        isFocused -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = TodayContainerAlpha)
        else -> Color.Transparent
    }
    val contentColor = when {
        isFocused -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val weekdayColor = when {
        isFocused -> MaterialTheme.colorScheme.onPrimary.copy(alpha = FocusedWeekdayAlpha)
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderModifier = if (isToday && !isFocused) {
        Modifier.border(
            BorderStroke(DayPillBorderWidth, MaterialTheme.colorScheme.primary.copy(alpha = TodayBorderAlpha)),
            PillShape
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .padding(horizontal = DayPillSpacing)
            .height(DayPillHeight)
            .clip(PillShape)
            .background(containerColor)
            .then(borderModifier)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "${date.dayOfWeek.name} ${date.month.name} ${date.day}"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = DayPillInnerVerticalPadding)
        ) {
            Text(
                text = date.dayOfWeek.name.take(1),
                style = MaterialTheme.typography.labelSmall,
                color = weekdayColor,
                fontWeight = if (isFocused || isToday) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(DayPillSpacingBetweenText))
            Text(
                text = date.day.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                fontWeight = if (isFocused || isToday) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}
