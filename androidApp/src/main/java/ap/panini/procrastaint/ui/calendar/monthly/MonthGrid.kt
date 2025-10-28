package ap.panini.procrastaint.ui.calendar.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.dayOfMonth
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
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
    tasks: List<TaskSingle>,
    onDateClick: (LocalDate) -> Unit
) {
    val monthDate = kotlin.time.Instant.fromEpochMilliseconds(month)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    val tasksByDay = tasks.groupBy { it.currentEventTime.dayOfMonth() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header row with day names
        Row(modifier = Modifier.fillMaxWidth()) {
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

        // Grid area
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val cellHeight = maxHeight / 6  // 6 rows = 42 / 7

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false // optional: make it static calendar grid
            ) {
                val firstDayOfMonth = LocalDate(monthDate.year, monthDate.month, 1)
                val startDayOfWeek = firstDayOfMonth.dayOfWeek.isoDayNumber % 7
                val gridStartDate = firstDayOfMonth.minus(startDayOfWeek.toLong(), DateTimeUnit.DAY)

                items(42) { index ->
                    val date = gridStartDate.plus(index.toLong(), DateTimeUnit.DAY)

                    Card(
                        modifier = Modifier
                            .padding(2.dp)
                            .height(cellHeight) // ⬅️ force equal height
                            .fillMaxWidth()
                            .clickable { onDateClick(date) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (date.month == monthDate.month) {
                            Column(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = date.day.toString())
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    tasksByDay[date.day]?.forEach { task ->
                                        val tagColor = task.tags.firstOrNull()?.toRgbOrNull()
                                        val color = if (tagColor != null) {
                                            Color(tagColor.first, tagColor.second, tagColor.third)
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        }
                                        Box(
                                            modifier = Modifier
                                                .padding(1.dp)
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(color)
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
