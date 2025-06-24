package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Parsed

/**
 * displays all the times of the auto parsed data
 * */
@Composable
fun TimeChips(
    currentAutoParsed: Parsed?,
    modifier: Modifier = Modifier,
    manualStart: Set<Long> = setOf(),
    manualEnd: Long? = null,
    removeManualStart: (Long) -> Unit = {},
    removeManualEnd: () -> Unit = {},
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        currentAutoParsed?.startTimes?.forEach { time ->
            InputChip(
                selected = true,
                onClick = { /* ignored */ },
                label = { Text(text = time.formatMilliseconds(currentAutoParsed.tagsTimeStart)) }
            )
        }

        manualStart.forEach { time ->
            InputChip(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Remove start time",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                selected = true,
                onClick = { removeManualStart(time) },
                label = {
                    Text(
                        text = time.formatMilliseconds(),
                        color = MaterialTheme.colorScheme.onSecondaryContainer

                    )
                },
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }

        if (manualEnd != null) {
            InputChip(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Remove end time",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                },
                selected = true,
                onClick = { removeManualEnd() },
                label = {
                    Text(
                        text = manualEnd.formatMilliseconds(),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                },
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            )
        } else {
            currentAutoParsed?.endTime?.let {
                InputChip(
                    selected = true,
                    onClick = { /* ignored */ },
                    label = {
                        Text(
                            text = it.formatMilliseconds(
                                currentAutoParsed.tagsTimeEnd
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            }
        }
    }
}
