package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskView(
    task: TaskSingle,
    onCheck: (TaskSingle) -> Unit,
    onEdit: (taskId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = { onEdit(task.taskId) },
                onClick = { onCheck(task) }
            ),

        trailingContent = {
            if (task.startTime != null) {
                Text(
                    task.currentEventTime.formatMilliseconds(
                        setOf(Time.HOUR, Time.MINUTE),
                        smart = false,
                        useAbbreviated = true
                    )
                )
            }
        },

        leadingContent = {
            Checkbox(
                checked = task.completed != null,
                onCheckedChange = {
                    onCheck(task)
                },
            )
        },

        supportingContent = {
            Column {
                if (task.repeatOften != null && task.repeatTag != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Repeat,
                            null,
                            modifier = Modifier.height(15.dp)
                        )
                        Text(
                            "${task.repeatOften} ${task.repeatTag!!.name}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                if (task.description.isNotBlank()) {
                    Text(
                        task.description,
                        modifier = Modifier.clickable { expanded = !expanded },
                        overflow = TextOverflow.Ellipsis,
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },

        headlineContent = {
            Text(task.title)
        }
    )
}
