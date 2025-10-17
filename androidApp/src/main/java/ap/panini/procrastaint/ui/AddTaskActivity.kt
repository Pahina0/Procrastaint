package ap.panini.procrastaint.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ap.panini.procrastaint.ui.theme.ProcrastaintTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddTaskActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        intent.getLongExtra("taskId", -1L).takeIf { it != -1L }?.let {
            viewModel.editCreatedTask(it)
        }

        setContent {
            ProcrastaintTheme {
                val state by viewModel.uiState.collectAsState()
                TaskBottomSheet(
                    state = state,
                    updateTitle = viewModel::updateTask,
                    updateDescription = viewModel::updateDescription,
                    viewNextParsed = viewModel::viewNextParsed,
                    onDismissRequest = { finishAndRemoveTask() },
                    getTagsStarting = viewModel::getTagsStarting,
                    saveTask = {
                        viewModel.save()
                        finishAndRemoveTask()
                    },
                    deleteTask = {
                        viewModel.deleteEditTask()
                        finishAndRemoveTask()
                    }
                )
            }
        }
    }
}
