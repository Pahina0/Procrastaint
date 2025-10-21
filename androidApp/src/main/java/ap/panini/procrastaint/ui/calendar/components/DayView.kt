package ap.panini.procrastaint.ui.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Date.toDayOfWeek
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.dayOfWeek

@Composable
fun DayView(
    date: Long,
    tasks: List<TaskSingle>,
    dateType: ViewingType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    date.formatMilliseconds(setOf(Time.DAY), useAbbreviated = true),
                    color = dateType.getTextColor()
                )
                val remaining = tasks.filter { it.completed == null }.size

                if (remaining > 0) {
                    Badge(
                        containerColor = dateType.getBadgeColor()
                    ) {
                        Text(
                            remaining.toString(),
                            color = dateType.getBadgeContentColor()
                        )
                    }
                }
            }

            Text(
                date.toDayOfWeek(),
                style = MaterialTheme.typography.labelSmall,
                color = if (date.dayOfWeek() == 6 || date.dayOfWeek() == 7) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Unspecified
                }
            )
        }
    }
}

sealed interface ViewingType {
    data object Selected : ViewingType
    data object Past : ViewingType
    data object Future : ViewingType
    data object Today : ViewingType
}

@Composable
private fun ViewingType.getTextColor() = with(MaterialTheme.colorScheme) {
    when (this@getTextColor) {
        is ViewingType.Past, ViewingType.Future -> onSurface
        is ViewingType.Today -> primary
        is ViewingType.Selected -> tertiary
    }
}

@Composable
private fun ViewingType.getBadgeColor() =
    with(MaterialTheme.colorScheme) {
        when (this@getBadgeColor) {
            is ViewingType.Past -> secondaryContainer
            is ViewingType.Future -> primaryContainer
            is ViewingType.Today -> errorContainer
            is ViewingType.Selected -> tertiaryContainer
        }
    }

@Composable
private fun ViewingType.getBadgeContentColor() =
    with(MaterialTheme.colorScheme) {
        when (this@getBadgeContentColor) {
            is ViewingType.Past -> onSecondaryContainer
            is ViewingType.Future -> onPrimaryContainer
            is ViewingType.Today -> onErrorContainer
            is ViewingType.Selected -> onTertiaryContainer
        }
    }
