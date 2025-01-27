package ap.panini.procrastaint.ui.upcoming

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.components.DateText
import ap.panini.procrastaint.ui.components.TaskView
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import org.koin.androidx.compose.koinViewModel

@Destination<RootGraph>
@Composable
fun UpcomingScreen(
    modifier: Modifier = Modifier,
    viewModel: UpcomingViewModel = koinViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LazyColumn(modifier = modifier.padding(10.dp)) {
        itemsIndexed(
            items = state.taskInfos,
//            key = { _, task -> /*Pair(task.taskId, task.currentEventTime)*/ }
        ) { index, task ->

            // sees if it should show the header of the current date
            if (index == 0 || state.taskInfos[index - 1].currentEventTime.formatMilliseconds(
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
                onCheck = viewModel::checkTask
            )
        }
    }
}
