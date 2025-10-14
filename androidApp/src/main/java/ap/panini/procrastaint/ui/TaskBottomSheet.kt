package ap.panini.procrastaint.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.ui.components.TaskTextField
import ap.panini.procrastaint.ui.components.TimeChips
import ap.panini.procrastaint.ui.components.rememberParsedText
import kotlinx.coroutines.launch

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
    val bottomSheetState = rememberModalBottomSheetState()
    val parsedTimes by remember(state.parsed?.times, state.viewing) {
        mutableStateOf(state.parsed?.times?.getOrNull(state.viewing))
    }

    var tagSuggestions by remember { mutableStateOf(listOf<TaskTag>()) }

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
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TaskTextField(
                text = state.task,
                suggestions = tagSuggestions,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { updateTitle(it) },
                label = { Text(text = "Whats on your mind?") },
                onCurrentWordChange = { tagSuggestions = getTagsStarting(it) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                trailingIcon = {
                    IconButton(onClick = { saveTask() }) {
                        when (state.mode) {
                            is MainActivityViewModel.MainUiState.Mode.Create -> {
                                Icon(
                                    imageVector = Icons.Outlined.TaskAlt,
                                    contentDescription = "Save task"
                                )
                            }

                            is MainActivityViewModel.MainUiState.Mode.Edit -> {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit task"
                                )
                            }
                        }
                    }
                }
            )

            HorizontalDivider()

            if (state.task.isNotBlank()) {
                Text(
                    text = rememberParsedText(
                        state.task,
                        state.parsed,
                        state.viewing,
                    ),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            TimeChips(parsedTimes, leadingContent = {
                ActionList(
                    viewNextParsed = viewNextParsed,
                    viewing = state.viewing,
                    currentViewingSize = state.parsed?.times?.size ?: 0
                )
            })

            OutlinedTextField(
                value = state.description,
                onValueChange = { updateDescription(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Description") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            if (state.mode is MainActivityViewModel.MainUiState.Mode.Edit) {
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.mode.repeated) {
                        OutlinedButton(
                            onClick = {
                                completeForeverTask()
                            },
                        ) {
                            Text("Complete Forever")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            deleteTask()
                        },
                        colors = ButtonDefaults.outlinedButtonColors().copy(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                    ) {
                        Text("Delete task")
                    }
                }
            }

            Text(
                text = rememberParsedText(
                    state.task,
                    state.parsed,
                    state.viewing,
                    show = false
                ),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * shows the actions such as adding a start time and modifying end time
 * */
@Composable
private fun ActionList(
    viewNextParsed: () -> Unit,
    viewing: Int,
    currentViewingSize: Int
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            AssistChip(onClick = { viewNextParsed() }, label = {
                Text(
                    text = when (viewing) {
                        -1 -> "No times found"
                        currentViewingSize -> "Ignored"
                        else -> "${viewing + 1}/$currentViewingSize"
                    }
                )
            }, leadingIcon = {
                Icon(
                    imageVector = if (viewing != -1) {
                        Icons.Outlined.Sync
                    } else {
                        Icons.Outlined.SyncDisabled
                    },
                    contentDescription = "Cycle through found times"
                )
            })
        }
    }
}
