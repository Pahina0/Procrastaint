package ap.panini.procrastaint.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.resources.Res
import ap.panini.procrastaint.resources.cycle_times
import ap.panini.procrastaint.resources.delete
import ap.panini.procrastaint.resources.delete_task
import ap.panini.procrastaint.resources.delete_task_message
import ap.panini.procrastaint.resources.delete_task_title
import ap.panini.procrastaint.resources.description_optional
import ap.panini.procrastaint.resources.description_placeholder
import ap.panini.procrastaint.resources.edit_task
import ap.panini.procrastaint.resources.ignored
import ap.panini.procrastaint.resources.new_task
import ap.panini.procrastaint.resources.no_time
import ap.panini.procrastaint.resources.save_changes
import ap.panini.procrastaint.resources.save_task
import ap.panini.procrastaint.resources.stop_repeating
import ap.panini.procrastaint.resources.stop_repeating_message
import ap.panini.procrastaint.resources.stop_repeating_task
import ap.panini.procrastaint.resources.stop_repeating_title
import ap.panini.procrastaint.resources.task_hint_examples
import ap.panini.procrastaint.resources.title_label
import ap.panini.procrastaint.resources.what_needs_to_be_done
import ap.panini.procrastaint.ui.components.TaskTextField
import ap.panini.procrastaint.ui.components.TimeChips
import ap.panini.procrastaint.ui.components.rememberParsedText
import ap.panini.procrastaint.ui.tag.components.DeleteConfirmationDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBottomSheet(
    state: MainActivityViewModel.MainUiState,
    updateTitle: (String) -> Unit,
    updateDescription: (String) -> Unit,
    viewNextParsed: () -> Unit,
    onDismissRequest: () -> Unit,
    getTagsStarting: (String) -> List<TaskTag>,
    saveTask: () -> Unit,
    deleteTask: () -> Unit,
    completeForeverTask: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val parsedTimes = remember(state.parsed?.times, state.viewing) {
        state.parsed?.times?.getOrNull(state.viewing)
    }

    var tagSuggestions by remember { mutableStateOf(listOf<TaskTag>()) }
    val hintExamples = stringArrayResource(Res.array.task_hint_examples)
    val randomPlaceholder = remember(hintExamples) {
        if (hintExamples.isNotEmpty()) hintExamples.random() else ""
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStopRepeatingDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            title = stringResource(Res.string.delete_task_title),
            text = stringResource(Res.string.delete_task_message),
            confirmButtonText = stringResource(Res.string.delete),
            onConfirm = {
                showDeleteDialog = false
                deleteTask()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showStopRepeatingDialog) {
        DeleteConfirmationDialog(
            title = stringResource(Res.string.stop_repeating_title),
            text = stringResource(Res.string.stop_repeating_message),
            confirmButtonText = stringResource(Res.string.stop_repeating),
            onConfirm = {
                showStopRepeatingDialog = false
                completeForeverTask()
            },
            onDismiss = { showStopRepeatingDialog = false }
        )
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = {
            scope.launch {
                bottomSheetState.hide()
            }.invokeOnCompletion {
                onDismissRequest()
            }
        },
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (state.mode) {
                        is MainActivityViewModel.MainUiState.Mode.Create -> stringResource(Res.string.new_task)
                        is MainActivityViewModel.MainUiState.Mode.Edit -> stringResource(Res.string.edit_task)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                if (state.mode is MainActivityViewModel.MainUiState.Mode.Edit) {
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(Res.string.delete_task),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Task Title Input
            TaskTextField(
                text = state.task,
                suggestions = tagSuggestions,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { updateTitle(it) },
                label = { Text(stringResource(Res.string.what_needs_to_be_done)) },
                placeholder = { Text(randomPlaceholder) },
                onCurrentWordChange = { tagSuggestions = getTagsStarting(it) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            // Detected Timing & Recurrence Chips
            TimeChips(
                currentAutoParsed = parsedTimes,
                leadingContent = {
                    val timesCount = state.parsed?.times?.size ?: 0
                    if (timesCount > 0) {
                        ActionList(
                            viewNextParsed = viewNextParsed,
                            viewing = state.viewing,
                            currentViewingSize = timesCount
                        )
                    }
                }
            )

            // Cleaned Task Preview (when date or tags are extracted)
            val cleanedText = rememberParsedText(
                state.task,
                state.parsed,
                state.viewing,
                show = false
            ).text

            if (state.task.isNotBlank() && cleanedText != state.task.trim() && cleanedText.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.title_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = cleanedText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Task Description
            OutlinedTextField(
                value = state.description,
                onValueChange = { updateDescription(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.description_optional)) },
                placeholder = { Text(stringResource(Res.string.description_placeholder)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            when (state.mode) {
                is MainActivityViewModel.MainUiState.Mode.Create -> {
                    Button(
                        onClick = { saveTask() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.task.isNotBlank(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(Res.string.save_task),
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                is MainActivityViewModel.MainUiState.Mode.Edit -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { saveTask() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.task.isNotBlank(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = stringResource(Res.string.save_changes),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        if (state.mode.repeated) {
                            OutlinedButton(
                                onClick = { showStopRepeatingDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.EventBusy,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = stringResource(Res.string.stop_repeating_task),
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * shows the actions to cycle through alternate parsed time interpretations
 */
@Composable
private fun ActionList(
    viewNextParsed: () -> Unit,
    viewing: Int,
    currentViewingSize: Int
) {
    AssistChip(
        onClick = { viewNextParsed() },
        label = {
            Text(
                text = when (viewing) {
                    -1 -> stringResource(Res.string.no_time)
                    currentViewingSize -> stringResource(Res.string.ignored)
                    else -> "${viewing + 1} of $currentViewingSize"
                },
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = if (viewing != -1 && viewing != currentViewingSize) {
                    Icons.Outlined.Sync
                } else {
                    Icons.Outlined.SyncDisabled
                },
                contentDescription = stringResource(Res.string.cycle_times),
                modifier = Modifier.size(16.dp)
            )
        }
    )
}
