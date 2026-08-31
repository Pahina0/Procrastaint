package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time

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

    val primaryTag = task.tags.firstOrNull()
    val primaryTagColor = primaryTag?.toRgbOrNull()?.let {
        Color(it.first, it.second, it.third)
    } ?: MaterialTheme.colorScheme.primary

    val accentColor = if (isDone) {
        MaterialTheme.colorScheme.outlineVariant
    } else {
        primaryTagColor
    }

    val groupName = primaryTag?.displayTitle?.ifBlank { primaryTag.title }

    val timeString = if (task.startTime != null) {
        val timeSet = mutableSetOf(Time.HOUR, Time.MINUTE)
        if (showFullDate) {
            timeSet += Time.DAY
        }
        task.currentEventTime.formatMilliseconds(
            timeSet,
            smart = showFullDate,
            useAbbreviated = true
        )
    } else {
        null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .combinedClickable(onClick = { onEdit(task) }),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDone -> MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f)
                task.tags.isNotEmpty() -> primaryTagColor.copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color bar indicator matching artistic event style
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Checkbox(
                checked = isDone,
                onCheckedChange = { onCheck(task) },
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Top Row: Title + Repeat Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (task.repeatOften != null && task.repeatTag != null) {
                        val repeatText = if (task.repeatOften == 1) {
                            "Every ${task.repeatTag!!.name.lowercase()}"
                        } else {
                            "Every ${task.repeatOften} ${task.repeatTag!!.name.lowercase()}s"
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isDone) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = repeatText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDone) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Bottom Row: Time (bottom-left) • Group name • Description
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (timeString != null) {
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isDone) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!groupName.isNullOrBlank()) {
                        if (timeString != null) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isDone) Color.Gray else primaryTagColor)
                        )

                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDone) Color.Gray else primaryTagColor,
                            maxLines = 1
                        )
                    }

                    if (task.description.isNotBlank()) {
                        if (timeString != null || !groupName.isNullOrBlank()) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Text(
                            text = task.description.trim(),
                            modifier = Modifier
                                .clickable { expanded = !expanded }
                                .weight(1f, fill = false),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = if (expanded) Int.MAX_VALUE else 1,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
