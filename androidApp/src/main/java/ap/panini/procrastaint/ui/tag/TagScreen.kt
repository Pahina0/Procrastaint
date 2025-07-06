package ap.panini.procrastaint.ui.tag

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.ui.components.TaskView
import ap.panini.procrastaint.ui.tag.components.DeleteConfirmationDialog
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun TagScreen(
    tagId: Long,
    modifier: Modifier = Modifier,
    viewModel: TagViewModel = koinViewModel { parametersOf(tagId) },
    destinationsNavigator: DestinationsNavigator,
) {
    val tasks = viewModel.tasks.collectAsStateWithLifecycle(emptyList()).value
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteWithTasksDialog by remember { mutableStateOf(false) }

    val activityViewModel = koinViewModel<MainActivityViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    ScreenScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(viewModel.tag.title) },
                navigationIcon = {
                    IconButton(onClick = { destinationsNavigator.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showMenu = true },
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showDeleteDialog = true
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete with tasks",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showDeleteWithTasksDialog = true
                                showMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            items(tasks) { task ->
                TaskView(
                    task = task,
                    onCheck = viewModel::checkTask,
                    onEdit = activityViewModel::editCreatedTask,
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            title = "Deleting tag",
            text = "Are you sure you want to delete this tag? This action cannot be undone.",
            confirmButtonText = "Delete",
            onConfirm = {
                viewModel.onDelete()
                showDeleteDialog = false
                destinationsNavigator.popBackStack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showDeleteWithTasksDialog) {
        DeleteConfirmationDialog(
            title = "Deleting tag + tasks",
            text = "Are you sure you want to delete this tag and all associated tasks? This action cannot be undone.",
            confirmButtonText = "Delete All",
            onConfirm = {
                viewModel.onDeleteWithTasks()
                showDeleteWithTasksDialog = false
                destinationsNavigator.popBackStack()
            },
            onDismiss = { showDeleteWithTasksDialog = false }
        )
    }
}
