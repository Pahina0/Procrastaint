package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.tooling.preview.Preview
import ap.panini.procrastaint.data.model.Task
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time

@Composable
fun TaskView(
    task: Task,
    onCheck: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.completed != null,
            onCheckedChange = {
                onCheck(task)
            },
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(task.title, style = MaterialTheme.typography.titleMedium)

            if (task.description.isNotBlank()) {
                Text(
                    task.description,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (task.startTime != null) {
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    task.startTime?.formatMilliseconds(setOf(Time.HOUR, Time.MINUTE), smart = false)
                        ?: ""
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewTaskView() {
    var task by remember {
        mutableStateOf(
            Task(
                title = "Do something soon",
                startTime = 2198049102284L
            )
        )
    }

    TaskView(
        task = task,
        onCheck = { task = it.copy(completed = 0L) },
        modifier = Modifier.fillMaxSize()
    )
}
