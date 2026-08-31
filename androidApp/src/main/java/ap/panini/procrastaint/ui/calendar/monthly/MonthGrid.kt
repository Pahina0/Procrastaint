package ap.panini.procrastaint.ui.calendar.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.dayOfMonth
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.ExperimentalTime

private const val DayOfWeekAbbreviationLength = 3
private const val WeeksInMonth = 6
private const val DaysInWeek = 7
private const val GridCellCount = WeeksInMonth * DaysInWeek

@OptIn(ExperimentalTime::class)
@Composable
fun MonthGrid(
    month: Long,
    tasks: List<TaskSingle>,
    onDateClick: (LocalDate) -> Unit,
    onDateFocus: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthDate = kotlin.time.Instant.fromEpochMilliseconds(month)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    val tasksByDay = tasks.groupBy { it.currentEventTime.dayOfMonth() }
    val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())

    Column(modifier = modifier.fillMaxSize()) {
        // Header row with day names
        Row(modifier = Modifier.fillMaxWidth()) {
            val daysOfWeek =
                listOf(DayOfWeek.SUNDAY) + DayOfWeek.entries.filter { it != DayOfWeek.SUNDAY }
            for (day in daysOfWeek) {
                Text(
                    text = day.name.take(DayOfWeekAbbreviationLength),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Grid area
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val cellHeight = maxHeight / WeeksInMonth // 6 rows = 42 / 7

            LazyVerticalGrid(
                columns = GridCells.Fixed(DaysInWeek),
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false // optional: make it static calendar grid
            ) {
                val firstDayOfMonth = LocalDate(monthDate.year, monthDate.month, 1)
                val startDayOfWeek = firstDayOfMonth.dayOfWeek.isoDayNumber % DaysInWeek
                val gridStartDate = firstDayOfMonth.minus(startDayOfWeek.toLong(), DateTimeUnit.DAY)

                items(GridCellCount) { index ->
                    val date = gridStartDate.plus(index.toLong(), DateTimeUnit.DAY)
                    val isInCurrentMonth = date.month == monthDate.month
                    val isToday = date == today

                    Card(
                        modifier = Modifier
                            .padding(1.5.dp)
                            .height(cellHeight)
                            .fillMaxWidth()
                            .then(
                                if (isInCurrentMonth) {
                                    Modifier.combinedClickable(
                                        onClick = { onDateClick(date) },
                                        onLongClick = { onDateFocus(date) }
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                isInCurrentMonth -> MaterialTheme.colorScheme.surfaceContainerLowest
                                else -> MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.3f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = date.day.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isToday) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                                color = when {
                                    isToday -> MaterialTheme.colorScheme.primary
                                    isInCurrentMonth -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                }
                            )

                            if (isInCurrentMonth) {
                                val dayTasks = tasksByDay[date.day]
                                if (!dayTasks.isNullOrEmpty()) {
                                    androidx.compose.foundation.layout.FlowRow(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalArrangement = Arrangement.spacedBy(1.dp)
                                    ) {
                                        dayTasks.take(6).forEach { task ->
                                            val dotColor = if (task.completed != null) {
                                                Color.Gray
                                            } else {
                                                task.tags.firstOrNull()?.toRgbOrNull()
                                                    ?.let { Color(it.first, it.second, it.third) }
                                                    ?: MaterialTheme.colorScheme.primary
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .padding(0.5.dp)
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
