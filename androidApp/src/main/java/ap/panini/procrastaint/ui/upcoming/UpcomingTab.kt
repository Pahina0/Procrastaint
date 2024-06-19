package ap.panini.procrastaint.ui.upcoming

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.data.model.Task
import ap.panini.procrastaint.ui.theme.SlightlyDeemphasizedAlpha
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

class UpcomingTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Upcoming"
            val icon = rememberVectorPainter(Icons.Outlined.Upcoming)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<UpcomingScreenModel>()
        val upcomingState by screenModel.tasks.collectAsStateWithLifecycle()

        LazyColumn {
            items(
                items = upcomingState,
                key = { it.id }
            ) {
                TaskItem(
                    task = it,
                    modifier = Modifier.animateItemPlacement(),
                    check = { screenModel.checkTask(it) }
                )
            }
        }
    }

    @Composable
    fun TaskItem(task: Task, modifier: Modifier = Modifier, check: () -> Unit) {
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
                                textDecoration = if (completed) TextDecoration.LineThrough else null
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
                checked = completed,
                onCheckedChange = {
                    completed = it
                    check()
                },

            )
        }
    }
}
