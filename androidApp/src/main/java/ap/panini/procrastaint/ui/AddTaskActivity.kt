package ap.panini.procrastaint.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.ui.theme.ProcrastaintTheme
import ap.panini.procrastaint.ui.widget.taskSingleKey
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddTaskActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.getStringExtra(taskSingleKey.name)?.let {
            val task = Json.decodeFromString<TaskSingle>(it)
            viewModel.editCreatedTask(task)
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
                    },
                    completeForeverTask = {
                        viewModel.removeFutureRepeatsEditTask()
                        finishAndRemoveTask()
                    }
                )
            }
        }
    }
}
