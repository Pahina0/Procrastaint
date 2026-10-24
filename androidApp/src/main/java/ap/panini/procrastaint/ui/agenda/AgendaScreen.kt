package ap.panini.procrastaint.ui.agenda

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.agenda.components.AgendaFeed
import ap.panini.procrastaint.ui.agenda.components.CollapsibleCalendarHeader
import ap.panini.procrastaint.ui.agenda.components.NeedsAttentionCard
import ap.panini.procrastaint.ui.agenda.components.TagChipRow
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.ui.components.TagBottomSheet
import ap.panini.procrastaint.ui.components.rememberBottomSheetTagState
import ap.panini.procrastaint.ui.navigation.Route
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SearchTextFieldCornerRadius = 16.dp
private val SearchHorizontalPadding = 16.dp
private val SearchVerticalPadding = 8.dp
private val NeedsAttentionHorizontalPadding = 16.dp
private val NeedsAttentionVerticalPadding = 4.dp

@Suppress("UnusedParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AgendaScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToTag: (Route.Tag) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = koinViewModel()
) {
    val activityViewModel = koinViewModel<MainActivityViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val recentlyCompleted by viewModel.recentlyCompleted.collectAsStateWithLifecycle()

    var isSearchActive by remember { mutableStateOf(false) }
    var showTagBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberBottomSheetTagState()

    val todayMillis = remember { ap.panini.procrastaint.util.Date.getTodayStart() }
    val todayFormatted = remember(todayMillis) {
        val formatter = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        formatter.format(Date(todayMillis))
    }

    val todayTasksRemaining = remember(
        state.scheduledTasksByDate,
        state.overdueTasks,
        todayMillis,
        recentlyCompleted
    ) {
        val scheduledRemaining = state.scheduledTasksByDate[todayMillis].orEmpty().count { task ->
            task.completed == null && Pair(task.taskId, task.currentEventTime) !in recentlyCompleted
        }
        val overdueRemaining = state.overdueTasks.count { task ->
            task.completed == null && Pair(task.taskId, task.currentEventTime) !in recentlyCompleted
        }
        scheduledRemaining + overdueRemaining
    }

    val tasksRemainingText = remember(todayTasksRemaining) {
        when (todayTasksRemaining) {
            0 -> "All caught up today"
            1 -> "1 task remaining"
            else -> "$todayTasksRemaining tasks remaining"
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    if (showTagBottomSheet) {
        TagBottomSheet(
            state = bottomSheetState,
            onDismissRequest = {
                bottomSheetState.randomReset()
                showTagBottomSheet = false
            },
            isValidTag = viewModel::isValidTag,
            onSave = { tag ->
                showTagBottomSheet = false
                bottomSheetState.randomReset()
                viewModel.saveTag(tag)
            }
        )
    }

    ScreenScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = null,
                                    indication = null,
                                    onClick = viewModel::scrollToToday
                                )
                        ) {
                            Text(
                                text = todayFormatted,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = tasksRemainingText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                isSearchActive = !isSearchActive
                                if (!isSearchActive) {
                                    viewModel.setSearchQuery("")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSearchActive) {
                                    Icons.Outlined.Close
                                } else {
                                    Icons.Outlined.Search
                                },
                                contentDescription = if (isSearchActive) {
                                    "Close search"
                                } else {
                                    "Search tasks"
                                }
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                )

                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        placeholder = { Text("Search tasks...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(SearchTextFieldCornerRadius),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = SearchHorizontalPadding,
                                vertical = SearchVerticalPadding
                            )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CollapsibleCalendarHeader(
                focusedDate = state.focusedDate,
                isMonthExpanded = state.isMonthExpanded,
                monthTasksByDate = state.monthTasksByDate,
                onSelectDate = viewModel::setFocusedDate,
                onToggleMonthExpansion = viewModel::toggleMonthExpansion
            )

            TagChipRow(
                tags = state.tags,
                selectedTagId = state.selectedTagId,
                onSelectTag = viewModel::selectTag,
                onEditTag = { tag ->
                    bottomSheetState.setTag(tag)
                    showTagBottomSheet = true
                },
                onAddTag = {
                    bottomSheetState.randomReset()
                    showTagBottomSheet = true
                }
            )

            NeedsAttentionCard(
                overdueTasks = state.overdueTasks,
                onMoveAllToToday = viewModel::moveAllOverdueToToday,
                onCheckTask = viewModel::checkTask,
                onEditTask = activityViewModel::editCreatedTask,
                modifier = Modifier.padding(
                    horizontal = NeedsAttentionHorizontalPadding,
                    vertical = NeedsAttentionVerticalPadding
                )
            )

            AgendaFeed(
                scheduledTasksByDate = state.scheduledTasksByDate,
                externalEventsByDate = state.externalEventsByDate,
                recentlyCompleted = recentlyCompleted,
                focusedDate = state.focusedDate,
                onCheckTask = viewModel::checkTask,
                onEditTask = activityViewModel::editCreatedTask,
                isFilterActive = state.selectedTagId != null || state.searchQuery.isNotBlank(),
                onFocusedDateChange = viewModel::setFocusedDate,
                scrollToTodayEvent = viewModel.scrollToTodayEvent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
