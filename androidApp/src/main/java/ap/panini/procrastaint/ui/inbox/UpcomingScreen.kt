package ap.panini.procrastaint.ui.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.data.model.Task
import ap.panini.procrastaint.ui.theme.SlightlyDeemphasizedAlpha
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun UpcomingScreen(
    modifier: Modifier = Modifier,
    viewModel: UpcomingViewModel = koinViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    LazyColumn {
        items(
            items = state.tasks,
            key = { it.id }
        ) {
            TaskItem(
                task = it,
                modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                check = viewModel::checkTask
            )
        }
    }
}

@Composable
fun TaskItem(task: Task, modifier: Modifier = Modifier, check: (Task) -> Unit) {
    var completed by remember {
        mutableStateOf(task.completed)
    }

    Row(
        modifier = modifier
            .padding(10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            task.apply {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.plus(
                        TextStyle(
                            textDecoration = if (completed != null) TextDecoration.LineThrough else null
                        )
                    ),
                )
                description?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = SlightlyDeemphasizedAlpha),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                getTimeRangeString().let {
                    if (it.isNotBlank()) {
                        Text(text = it)
                    }
                }
            }
        }

        Checkbox(
            checked = completed != null,
            onCheckedChange = {
                check(task)
                completed = task.completed
            },

        )
    }
}
