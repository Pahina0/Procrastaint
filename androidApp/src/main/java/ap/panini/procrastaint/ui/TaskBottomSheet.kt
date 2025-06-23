package ap.panini.procrastaint.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.ui.Action.ACTION_ADD_END
import ap.panini.procrastaint.ui.Action.ACTION_ADD_START
import ap.panini.procrastaint.ui.Action.ACTION_NONE
import ap.panini.procrastaint.ui.components.TimePickerDialog
import ap.panini.procrastaint.util.Date
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Parsed
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.dayOfMonth
import ap.panini.procrastaint.util.hour
import ap.panini.procrastaint.util.minute
import ap.panini.procrastaint.util.month
import ap.panini.procrastaint.util.year
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

private enum class Action {
    ACTION_NONE, ACTION_ADD_START, ACTION_ADD_END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBottomSheet(
    state: MainActivityViewModel.MainUiState,
    updateTitle: (String) -> Unit,
    updateDescription: (String) -> Unit,
    viewNextParsed: () -> Unit,
    onDismissRequest: () -> Unit,
    editManualStartTime: (Long) -> Unit,
    editEndTime: (Long?) -> Unit,
    setRepeatTag: (Time?) -> Unit,
    setRepeatOften: (Int?) -> Unit,
    saveTask: () -> Unit,
    deleteTask: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()
    val currentAutoParsed = state.autoParsed.getOrNull(state.viewing)

    var selectedAction by remember { mutableStateOf(ACTION_NONE) }

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
            val task = buildAnnotatedString {
                val range = state.autoParsed.getOrNull(state.viewing)?.extractedRange

                if (range == null) {
                    append(state.task)
                    return@buildAnnotatedString
                }

                append(state.task.substring(0, range.first))
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(state.task.substring(range))
                }

                append(state.task.substring(range.last + 1))
            }

            // input task
            OutlinedTextField(
                value = state.task,
                onValueChange = { updateTitle(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Whats on your mind?") },
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

            if (state.task.isNotBlank()) {
                Text(task, style = MaterialTheme.typography.labelSmall)
            }

            ActionList(
                setAction = { selectedAction = it },
                viewNextParsed = viewNextParsed,
                viewing = state.viewing,
                currentViewingSize = state.autoParsed.size
            )

            ActionDisplay(
                selectedAction,
                state.endTime,
                currentAutoParsed?.endTime,
                editEndTime,
                editManualStartTime,
                setAction = { selectedAction = it }
            )

            TimeChips(
                currentAutoParsed,
                state.manualStartTimes,
                state.endTime,
                editManualStartTime,
                { editEndTime(null) }
            )

            RepeatTime(
                repeatOften = state.repeatInterval ?: currentAutoParsed?.repeatOften,
                repeatTag = state.repeatTag ?: currentAutoParsed?.repeatTag ?: Time.DAY,
                setRepeatOften = setRepeatOften,
                setRepeatTag = setRepeatTag
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { updateDescription(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Description") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            if (state.mode is MainActivityViewModel.MainUiState.Mode.Edit) {
                OutlinedButton(
                    onClick = {
                        deleteTask()
                    },
                    colors = ButtonDefaults.outlinedButtonColors().copy(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Delete task")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatTime(
    repeatOften: Int?,
    repeatTag: Time,
    setRepeatOften: (Int?) -> Unit,
    setRepeatTag: (Time?) -> Unit
) {
    var repeatTagExpanded by remember { mutableStateOf(false) }
    val timeOptions = mapOf(
        null to "auto",
        Time.YEAR to "year",
        Time.MONTH to "month",
        Time.WEEK to "week",
        Time.DAY to "day",
        Time.HOUR to "hour",
        Time.MINUTE to "minute"
    )

    val pattern = remember { Regex("^\\d+\$") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Repeating every")

        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = repeatOften?.toString() ?: "",
            onValueChange = {
                if (it.isEmpty() || it.matches(pattern)) {
                    setRepeatOften(it.toIntOrNull())
                }
            },
            singleLine = true,
            placeholder = { Text(text = "0") },
            label = { Text(text = "Interval") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        ExposedDropdownMenuBox(
            modifier = Modifier.weight(1f),
            expanded = repeatTagExpanded,
            onExpandedChange = { repeatTagExpanded = it }
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                value = timeOptions.getOrDefault(repeatTag, "auto"),
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text(text = "Time") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatTagExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )

            ExposedDropdownMenu(
                expanded = repeatTagExpanded,
                onDismissRequest = { repeatTagExpanded = false },
            ) {
                timeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.value,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            setRepeatTag(option.key)
                            repeatTagExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionDisplay(
    selectedAction: Action,
    manualEnd: Long?,
    parsedEnd: Long?,
    editEndTime: (Long?) -> Unit,
    editManualStartTime: (Long) -> Unit,
    setAction: (Action) -> Unit
) {
    if (selectedAction == ACTION_NONE) return

    HorizontalDivider()

    TimePickerRow(
        title = when (selectedAction) {
            ACTION_ADD_START -> "Start time"
            ACTION_ADD_END -> "End time"
            else -> "You shouldn't be seeing this now"
        },
        startTime = when (selectedAction) {
            ACTION_ADD_START -> Date.getTime()
            ACTION_ADD_END ->
                manualEnd ?: parsedEnd
                ?: Date.getTime()

            else -> Date.getTime()
        },
        onSave = {
            when (selectedAction) {
                ACTION_ADD_START -> editManualStartTime(it)
                ACTION_ADD_END -> editEndTime(it)
                else -> {
                    /* ignored */
                }
            }
        },
        onDismissRequest = { setAction(ACTION_NONE) }
    )

    HorizontalDivider()
}

/**
 * shows the actions such as adding a start time and modifying end time
 * */
@Composable
private fun ActionList(
    setAction: (Action) -> Unit,
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
                        currentViewingSize -> "Manual"
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

        item {
            AssistChip(
                onClick = { setAction(ACTION_ADD_START) },
                label = { Text(text = "Add start time") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.EditCalendar,
                        contentDescription = "Add start time"
                    )
                }
            )
        }

        item {
            AssistChip(
                onClick = { setAction(ACTION_ADD_END) },
                label = { Text(text = "Edit end time") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.EditCalendar,
                        contentDescription = "Edit end time"
                    )
                }
            )
        }
    }
}

/**
 * displays all the times of the auto parsed data
 * */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeChips(
    currentAutoParsed: Parsed?,
    manualStart: Set<Long>,
    manualEnd: Long?,
    removeManualStart: (Long) -> Unit,
    removeManualEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        currentAutoParsed?.startTimes?.forEach { time ->
            InputChip(
                selected = true,
                onClick = { /* ignored */ },
                label = { Text(text = time.formatMilliseconds(currentAutoParsed.tagsTimeStart)) }
            )
        }

        manualStart.forEach { time ->
            InputChip(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Remove start time"
                    )
                },
                selected = true,
                onClick = { removeManualStart(time) },
                label = { Text(text = time.formatMilliseconds()) },
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }

        if (manualEnd != null) {
            InputChip(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Remove end time"
                    )
                },
                selected = true,
                onClick = { removeManualEnd() },
                label = {
                    Text(
                        text = manualEnd.formatMilliseconds()
                    )
                },
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            )
        } else {
            currentAutoParsed?.endTime?.let {
                InputChip(
                    selected = true,
                    onClick = { /* ignored */ },
                    label = {
                        Text(
                            text = it.formatMilliseconds(
                                currentAutoParsed.tagsTimeEnd
                            )
                        )
                    },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerRow(
    title: String,
    startTime: Long,
    onSave: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var time by remember { mutableLongStateOf(startTime) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    CurrentDatePicker(
        startTime = time,
        enabled = showDatePicker,
        close = { showDatePicker = false },
        onSave = { year, month, dayOfMonth ->
            time = Date.getTime(
                year, month, dayOfMonth, time.hour(), time.minute()
            )
        }
    )

    CurrentTimePicker(
        hour = time.hour(),
        minute = time.minute(),
        enabled = showTimePicker,
        close = { showTimePicker = false },
        onSave = { hour, minute ->
            time = Date.getTime(
                time.year(), time.month(), time.dayOfMonth(), hour, minute
            )
        }
    )

    Column(
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontWeight = FontWeight.Bold)
            IconButton(onClick = { onDismissRequest() }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close time picker")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { showDatePicker = true }) {
                Text(
                    text = time.formatMilliseconds(
                        setOf(Time.YEAR, Time.MONTH, Time.DAY),
                        smart = false
                    )
                )
            }

            TextButton(onClick = { showTimePicker = true }) {
                Text(
                    text = time.formatMilliseconds(
                        setOf(Time.HOUR, Time.MINUTE),
                        smart = false
                    )
                )
            }
        }

        Button(onClick = { onSave(time) }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Add")
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun CurrentDatePicker(
    startTime: Long,
    enabled: Boolean,
    close: () -> Unit,
    onSave: (year: Int, month: Int, dayOfMonth: Int) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = startTime
    )

    if (enabled) {
        DatePickerDialog(onDismissRequest = { close() }, confirmButton = {
            Button(onClick = {
                // gets time in ms utc time zone and extracts year, month, day
                Calendar.getInstance().apply {
                    timeInMillis = datePickerState.selectedDateMillis!!
                    timeZone = TimeZone.getTimeZone("UTC")
                }.run {
                    // calendar month starts index 0 so + 1
                    onSave(get(Calendar.YEAR), get(Calendar.MONTH) + 1, get(Calendar.DAY_OF_MONTH))
                }
                close()
            }) {
                Text(text = "Save")
            }
        }) {
            DatePicker(state = datePickerState)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun CurrentTimePicker(
    hour: Int,
    minute: Int,
    enabled: Boolean,
    close: () -> Unit,
    onSave: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(hour, minute)

    if (enabled) {
        TimePickerDialog(onCancel = { close() }, onConfirm = {
            onSave(timePickerState.hour, timePickerState.minute)
            close()
        }) { displayMode ->
            if (displayMode == DisplayMode.Input) {
                TimePicker(state = timePickerState)
            } else {
                TimeInput(state = timePickerState)
            }
        }
    }
}
