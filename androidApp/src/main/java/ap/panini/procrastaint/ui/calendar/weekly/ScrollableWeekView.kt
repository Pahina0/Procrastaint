package ap.panini.procrastaint.ui.calendar.weekly

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.hour
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val HoursInDay = 24
private const val DayOfWeekAbbreviationLength = 3
private val HourHeight = 56.dp
private val TimeColWidth = 48.dp

@OptIn(ExperimentalTime::class)
@Composable
fun ScrollableWeekView(
    weekDates: List<LocalDate>,
    tasksByDate: Map<LocalDate, List<TaskSingle>>,
    eventsByDate: Map<LocalDate, List<ExternalCalendarEvent>>,
    today: LocalDate,
    currentHour: Int,
    onCellClick: (LocalDate, Int) -> Unit,
    onCellFocus: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        if (currentHour > 2) {
            listState.scrollToItem(currentHour - 2)
        }
    }

    Column(modifier = modifier) {
        // Day Names Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Spacer(modifier = Modifier.width(TimeColWidth))
            weekDates.forEach { date ->
                val isToday = date == today
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            state = listState
        ) {
            items(HoursInDay) { hour ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HourHeight)
                ) {
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

                    weekDates.forEach { date ->
                        val isCurrentCell = date == today && hour == currentHour
                        val tasksForHour = tasksByDate[date]?.filter { it.currentEventTime.hour() == hour } ?: emptyList()
                        val eventsForHour = eventsByDate[date]?.filter {
                            Instant.fromEpochMilliseconds(it.startTime).toLocalDateTime(TimeZone.currentSystemDefault()).hour == hour
                        } ?: emptyList()

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(HourHeight)
                                .padding(1.dp)
                                .combinedClickable(
                                    onClick = { onCellClick(date, hour) },
                                    onLongClick = { onCellFocus(date) }
                                ),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentCell) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                }
                            )
                        ) {
                            if (tasksForHour.isNotEmpty() || eventsForHour.isNotEmpty()) {
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(3.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    eventsForHour.forEach { event ->
                                        val color = event.color
                                        val eventColor = if (color != null && color != 0) {
                                            Color(color)
                                        } else {
                                            MaterialTheme.colorScheme.tertiary
                                        }
                                        Box(
                                            modifier = Modifier
                                                .padding(1.dp)
                                                .size(6.dp)
                                                .background(eventColor, CircleShape)
                                        )
                                    }

                                    tasksForHour.forEach { task ->
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
                                                .size(6.dp)
                                                .background(dotColor, CircleShape)
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
