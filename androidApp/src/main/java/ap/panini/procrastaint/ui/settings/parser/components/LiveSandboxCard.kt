package ap.panini.procrastaint.ui.settings.parser.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Parsed

private const val MaxInputLines = 3

private val SandboxSuggestions = listOf(
    "tomorrow morning",
    "04/05 at 8",
    "gym every other Tuesday at 5pm #health"
)

@Composable
fun LiveSandboxCard(
    input: String,
    parsed: Parsed?,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Live Parser Sandbox",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Type a sentence (e.g. Meeting tomorrow morning)") },
                singleLine = false,
                maxLines = MaxInputLines
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SandboxSuggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { onInputChange(suggestion) },
                        label = { Text(suggestion) }
                    )
                }
            }

            HorizontalDivider()

            val hasTimes = parsed != null && parsed.times.isNotEmpty()
            val hasTags = parsed != null && parsed.tags.isNotEmpty()

            if (parsed == null || (!hasTimes && !hasTags)) {
                Text(
                    text = "No date/time phrases detected yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hasTimes) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            parsed.times.forEach { time ->
                                val endTime = time.endTime
                                val formattedTime = if (time.startTimes.isNotEmpty()) {
                                    time.startTimes.joinToString(", ") { ts ->
                                        ts.formatMilliseconds(time.tagsTimeStart)
                                    }
                                } else if (endTime != null) {
                                    endTime.formatMilliseconds(time.tagsTimeEnd)
                                } else {
                                    ""
                                }
                                val labelText = if (formattedTime.isNotBlank()) {
                                    "${time.text} ($formattedTime)"
                                } else {
                                    time.text
                                }
                                AssistChip(
                                    onClick = {},
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    label = { Text(labelText) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                            alpha = 0.5f
                                        ),
                                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    border = null
                                )
                            }
                        }
                    }

                    if (hasTags) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            parsed.tags.forEach { tag ->
                                AssistChip(
                                    onClick = {},
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Tag,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    },
                                    label = { Text("#${tag.tag.title}") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(
                                            alpha = 0.5f
                                        ),
                                        labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    border = null
                                )
                            }
                        }
                    }

                    val cleanedTitle = parsed.textExtracted(0)
                    if (cleanedTitle.isNotBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cleaned title:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = cleanedTitle,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
