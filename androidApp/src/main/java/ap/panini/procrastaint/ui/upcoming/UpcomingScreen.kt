package ap.panini.procrastaint.ui.upcoming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.ui.components.DateText
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.ui.components.TaskView
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
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

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    ScreenScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text("Upcoming") }
            )
        }
    ) { padding ->
        if (state.taskInfos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Checklist,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
                Text("You have no upcoming tasks!")
                Text("Press + to start adding")
            }
        }

        Tasks(state.taskInfos, viewModel::checkTask, modifier = Modifier.padding(padding))
    }
}

@Composable
private fun Tasks(
    tasks: List<TaskSingle>,
    onCheck: (TaskSingle) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.padding(10.dp)) {
        itemsIndexed(
            items = tasks,
            key = { _, task -> Pair(task.taskId, task.currentEventTime) }
        ) { index, task ->

            // sees if it should show the header of the current date
            if (index == 0 || tasks[index - 1].currentEventTime.formatMilliseconds(
                    setOf(
                        Time.DAY, Time.MONTH, Time.YEAR
                    )
                ) != task.currentEventTime.formatMilliseconds(
                    setOf(
                        Time.DAY, Time.MONTH, Time.YEAR
                    )
                )

            ) {
                DateText(task.currentEventTime)
                HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))
            }
            TaskView(
                task = task,
                modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                onCheck = onCheck
            )
        }
    }
}
