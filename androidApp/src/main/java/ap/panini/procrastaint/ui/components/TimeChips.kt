package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.ParsedTime

/**
 * displays all the times of the auto parsed data
 * */
@Composable
fun TimeChips(
    currentAutoParsed: ParsedTime?,
    modifier: Modifier = Modifier,
    leadingContent: @Composable FlowRowScope.() -> Unit = {}
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        leadingContent()

        currentAutoParsed?.startTimes?.forEach { time ->
            InputChip(
                selected = true,
                onClick = { /* ignored */ },
                label = { Text(text = time.formatMilliseconds(currentAutoParsed.tagsTimeStart)) }
            )
        }

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

        currentAutoParsed?.let { time ->
            if (time.repeatOften == 0 || time.repeatTag == null) return@let

            InputChip(
                leadingIcon = { Icon(Icons.Outlined.Repeat, contentDescription = null) },
                selected = true,
                onClick = { /* ignored */ },
                label = { Text(text = "${time.repeatOften} ${time.repeatTag}(s)") }
            )
        }
    }
}
