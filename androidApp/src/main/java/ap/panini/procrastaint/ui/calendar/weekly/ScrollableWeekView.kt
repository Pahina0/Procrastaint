package ap.panini.procrastaint.ui.calendar.weekly

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.Date
import ap.panini.procrastaint.util.hour
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.char

private const val HoursInDay = 24
private const val DayOfWeekAbbreviationLength = 3
private val HourHeight = 60.dp
private val TimeColWidth = 50.dp

@Composable
fun ScrollableWeekView(
    weekData: List<Pair<LocalDate, List<TaskSingle>>>,
    today: LocalDate,
    currentHour: Int,
    onCellClick: (LocalDate, Int) -> Unit,
    onCellFocus: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (currentHour > 2) {
            listState.scrollToItem(currentHour - 2)
        }
    }

    val tasksByDayAndHour = remember(weekData) {
        weekData.associate { (date, tasks) ->
            date to tasks.groupBy { it.currentEventTime.hour() }
        }
    }

    Column(modifier = modifier) {
        // Day Names Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Spacer(modifier = Modifier.width(TimeColWidth))
            weekData.forEach { (date, _) ->
                val isToday = date == today
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfWeek.name.take(DayOfWeekAbbreviationLength),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = date.day.toString(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isToday) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            items(HoursInDay) { hour ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HourHeight)
                ) {
                    // Time label
                    val amPmFormatter = LocalTime.Format {
                        amPmHour()
                        char(' ')
                        amPmMarker("AM", "PM")
                    }
                    val localTime = LocalTime(hour, 0, 0)
                    val formattedTime = amPmFormatter.format(localTime)
                    Text(
                        text = formattedTime,
                        modifier = Modifier
                            .width(TimeColWidth)
                            .padding(end = 4.dp, top = 2.dp),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Day cells
                    weekData.forEach { (date, _) ->
                        val isCurrentCell = date == today && hour == currentHour
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(HourHeight)
                                .padding(1.dp)
                                .combinedClickable(
                                    onClick = { onCellClick(date, hour) },
                                    onLongClick = { onCellFocus(date) }
                                ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentCell) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                }
                            )
                        ) {
                            val tasks = tasksByDayAndHour[date]?.get(hour)
                            if (!tasks.isNullOrEmpty()) {
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    tasks.forEach { task ->
                                        val dotColor = if (task.completed != null) {
                                            Color.Gray
                                        } else {
                                            task.tags.firstOrNull()?.toRgbOrNull()
                                                ?.let { Color(it.first, it.second, it.third) }
                                                ?: MaterialTheme.colorScheme.primary
                                        }
                                        Box(
                                            modifier = Modifier
                                                .padding(1.dp)
                                                .size(7.dp)
                                                .background(
                                                    dotColor,
                                                    androidx.compose.foundation.shape.CircleShape
                                                )
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
