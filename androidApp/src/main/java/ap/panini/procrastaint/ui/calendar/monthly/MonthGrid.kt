package ap.panini.procrastaint.ui.calendar.monthly

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.dayOfMonth
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun MonthGrid(
    month: Long,
    tasks: List<TaskSingle>
) {
    val monthDate = kotlin.time.Instant.fromEpochMilliseconds(month)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    val lastDay = LocalDate(monthDate.year, monthDate.month, 1)
        .plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
    val daysInMonth = lastDay.day
    val firstDayOfWeek = LocalDate(monthDate.year, monthDate.month, 1).dayOfWeek

    val days = (1..daysInMonth).toList()
    val tasksByDay = tasks.groupBy { it.currentEventTime.dayOfMonth() }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            val daysOfWeek =
                listOf(DayOfWeek.SUNDAY) + DayOfWeek.entries.filter { it != DayOfWeek.SUNDAY }
            for (day in daysOfWeek) {
                Text(
                    text = day.name.take(3),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            val startPadding = (firstDayOfWeek.isoDayNumber % 7)
            items(startPadding) {
                // Empty cells for padding
            }
            items(days.size) { day ->
                Column(
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(text = (day + 1).toString())
                    tasksByDay[day + 1]?.forEach {
                        Text(text = it.title)
                    }
                }
            }
        }
    }
}
