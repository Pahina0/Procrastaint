package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time

@Composable
fun TasksMiniPreview(
    date: Long,
    tasks: List<TaskSingle>,
    dateType: ViewingType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(50.dp),
        shape = RoundedCornerShape(50.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)

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
            is ViewingType.Today -> error
            is ViewingType.Selected -> tertiaryContainer
        }
    }

@Composable
private fun ViewingType.getBadgeContentColor() =
    with(MaterialTheme.colorScheme) {
        when (this@getBadgeContentColor) {
            is ViewingType.Past -> onSecondaryContainer
            is ViewingType.Future -> onPrimaryContainer
            is ViewingType.Today -> onError
            is ViewingType.Selected -> onTertiaryContainer
        }
    }
