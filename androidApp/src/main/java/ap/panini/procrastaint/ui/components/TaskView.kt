package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskView(
    task: TaskSingle,
    onCheck: (TaskSingle) -> Unit,
    onEdit: (taskId: TaskSingle) -> Unit,
    modifier: Modifier = Modifier,
    showFullDate: Boolean = false,
    recentlyCompleted: Set<Pair<Long, Long>> = emptySet()
) {
    var expanded by remember { mutableStateOf(false) }
    val isDone = task.completed != null || recentlyCompleted.contains(
        Pair(task.taskId, task.currentEventTime)
    )

    Column(modifier = modifier) {
        ListItem(
            modifier = Modifier
                .combinedClickable(
                    onClick = { onEdit(task) }
                ),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            trailingContent = {
                if (task.startTime != null) {
                    val timeSet = mutableSetOf(Time.HOUR, Time.MINUTE)
                    if (showFullDate) {
                        timeSet += Time.DAY
                    }

                    Text(
                        text = task.currentEventTime.formatMilliseconds(
                            timeSet,
                            smart = showFullDate,
                            useAbbreviated = true
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            leadingContent = {
                Checkbox(
                    checked = isDone,
                    onCheckedChange = {
                        onCheck(task)
                    },
                )
            },
            headlineContent = {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
            },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (task.repeatOften != null && task.repeatTag != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            val repeatText = if (task.repeatOften == 1) {
                                "Every ${task.repeatTag!!.name.lowercase()}"
                            } else {
                                "Every ${task.repeatOften} ${task.repeatTag!!.name.lowercase()}s"
                            }
                            Text(
                                text = repeatText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            modifier = Modifier.clickable { expanded = !expanded },
                            overflow = TextOverflow.Ellipsis,
                            maxLines = if (expanded) Int.MAX_VALUE else 1,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (task.tags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            task.tags.forEach { tag ->
                                val tagColor = tag.toRgbOrNull()?.let {
                                    Color(it.first, it.second, it.third)
                                } ?: MaterialTheme.colorScheme.primary

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(tagColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(tagColor)
                                    )
                                    Text(
                                        text = tag.displayTitle.ifBlank { tag.title },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tagColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color.copy(alpha = 0.4f)
        )
    }
}
