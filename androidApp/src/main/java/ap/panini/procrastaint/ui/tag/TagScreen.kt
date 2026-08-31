package ap.panini.procrastaint.ui.tag

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.resources.Res
import ap.panini.procrastaint.resources.delete_all_confirm
import ap.panini.procrastaint.resources.delete_tag_confirm
import ap.panini.procrastaint.resources.delete_tag_title
import ap.panini.procrastaint.resources.delete_with_tasks_title
import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.components.EmptyPage
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.ui.components.TaskView
import ap.panini.procrastaint.ui.navigation.Route
import ap.panini.procrastaint.ui.tag.components.DeleteConfirmationDialog
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagScreen(
    args: Route.Tag,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TagViewModel = koinViewModel { parametersOf(args.tagId) },
) {
    val incompleteTasks = viewModel.incompleteTasks.collectAsStateWithLifecycle(emptyList()).value
    val completedTasks = viewModel.completedTasks.collectAsStateWithLifecycle(emptyList()).value
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteWithTasksDialog by remember { mutableStateOf(false) }

    val activityViewModel = koinViewModel<MainActivityViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    ScreenScaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        viewModel.tag.displayTitle.ifBlank { viewModel.tag.title }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
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
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            text = { Text("Delete Tag", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showDeleteDialog = true
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.FolderDelete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            text = {
                                Text(
                                    "Delete Tag & Tasks",
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
        if (incompleteTasks.isEmpty() && completedTasks.isEmpty()) {
            EmptyPage(
                icon = Icons.Outlined.CheckCircleOutline,
                text = "No tasks for this tag yet!\nAdd tasks using #${viewModel.tag.title}",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                if (incompleteTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "To Do (${incompleteTasks.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                    items(incompleteTasks, key = { it.taskId }) { task ->
                        TaskView(
                            task = task,
                            onCheck = viewModel::checkTask,
                            onEdit = activityViewModel::editCreatedTask,
                            showFullDate = true
                        )
                    }
                }

                if (completedTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Completed (${completedTasks.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    items(completedTasks.asReversed(), key = { it.taskId }) { task ->
                        TaskView(
                            task = task,
                            onCheck = viewModel::checkTask,
                            onEdit = activityViewModel::editCreatedTask,
                            showFullDate = true
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            title = stringResource(Res.string.delete_tag_title),
            text = "Are you sure you want to delete #${viewModel.tag.title}? Associated tasks will not be deleted.",
            confirmButtonText = stringResource(Res.string.delete_tag_confirm),
            onConfirm = {
                viewModel.onDelete()
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showDeleteWithTasksDialog) {
        DeleteConfirmationDialog(
            title = stringResource(Res.string.delete_with_tasks_title),
            text = "Are you sure you want to delete #${viewModel.tag.title} " +
                "and all associated tasks? This action cannot be undone.",
            confirmButtonText = stringResource(Res.string.delete_all_confirm),
            onConfirm = {
                viewModel.onDeleteWithTasks()
                showDeleteWithTasksDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteWithTasksDialog = false }
        )
    }
}
