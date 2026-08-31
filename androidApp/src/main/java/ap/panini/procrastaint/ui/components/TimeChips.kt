package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.ParsedTime

/**
 * displays all the times of the auto parsed data
 */
@Composable
fun TimeChips(
    currentAutoParsed: ParsedTime?,
    modifier: Modifier = Modifier,
    leadingContent: @Composable FlowRowScope.() -> Unit = {}
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        leadingContent()

        currentAutoParsed?.startTimes?.forEach { time ->
            AssistChip(
                onClick = { },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = {
                    Text(
                        text = time.formatMilliseconds(currentAutoParsed.tagsTimeStart),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                border = null
            )
        }

        currentAutoParsed?.endTime?.let {
            AssistChip(
                onClick = { },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                },
                label = {
                    Text(
                        text = it.formatMilliseconds(currentAutoParsed.tagsTimeEnd),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                border = null
            )
        }

        currentAutoParsed?.let { time ->
            val repeatTag = time.repeatTag
            if (time.repeatOften == 0 || repeatTag == null) return@let

            val repeatLabel = if (time.repeatOften == 1) {
                "Every ${repeatTag.name.lowercase()}"
            } else {
                "Every ${time.repeatOften} ${repeatTag.name.lowercase()}s"
            }

            AssistChip(
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Repeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = { },
                label = {
                    Text(
                        text = repeatLabel,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = null
            )
        }
    }
}
