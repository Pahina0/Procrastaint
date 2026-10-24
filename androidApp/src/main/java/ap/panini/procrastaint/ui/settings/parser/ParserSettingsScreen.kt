package ap.panini.procrastaint.ui.settings.parser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.data.models.DateFormatMode
import ap.panini.procrastaint.data.models.TimeFormatMode
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.ui.settings.components.Section
import ap.panini.procrastaint.ui.settings.components.SettingsItem
import ap.panini.procrastaint.ui.settings.parser.components.ComparisonDiffCard
import ap.panini.procrastaint.ui.settings.parser.components.HourPickerDialog
import ap.panini.procrastaint.ui.settings.parser.components.LiveSandboxCard
import ap.panini.procrastaint.ui.settings.parser.components.formatHour
import org.koin.androidx.compose.koinViewModel

private val HorizontalSectionPadding = 16.dp
private val SectionDividerPadding = 12.dp
private val ScreenBottomPadding = 24.dp
private val FormatsSpacing = 16.dp
private val ControlItemSpacing = 6.dp
private val CardVerticalPadding = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParserSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ParserSettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var activeHourPicker by rememberSaveable { mutableStateOf<String?>(null) }

    when (activeHourPicker) {
        "morning" -> {
            HourPickerDialog(
                title = "Set Morning Hour",
                initialHour = state.morningHour,
                use24 = state.use24,
                onHourSelected = { hour ->
                    viewModel.setMorningHour(hour)
                    activeHourPicker = null
                },
                onDismiss = { activeHourPicker = null }
            )
        }

        "afternoon" -> {
            HourPickerDialog(
                title = "Set Afternoon Hour",
                initialHour = state.afternoonHour,
                use24 = state.use24,
                onHourSelected = { hour ->
                    viewModel.setAfternoonHour(hour)
                    activeHourPicker = null
                },
                onDismiss = { activeHourPicker = null }
            )
        }

        "evening" -> {
            HourPickerDialog(
                title = "Set Evening Hour",
                initialHour = state.eveningHour,
                use24 = state.use24,
                onHourSelected = { hour ->
                    viewModel.setEveningHour(hour)
                    activeHourPicker = null
                },
                onDismiss = { activeHourPicker = null }
            )
        }

        "night" -> {
            HourPickerDialog(
                title = "Set Night Hour",
                initialHour = state.nightHour,
                use24 = state.use24,
                onHourSelected = { hour ->
                    viewModel.setNightHour(hour)
                    activeHourPicker = null
                },
                onDismiss = { activeHourPicker = null }
            )
        }
    }

    ScreenScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Date & Time") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::resetToDefaults) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = "Reset to defaults"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(bottom = ScreenBottomPadding)
        ) {
            Section("Time of Day Hours") {
                SettingsItem(
                    icon = Icons.Outlined.WbSunny,
                    text = "Morning",
                    subtext = formatHour(state.morningHour, state.use24),
                    onClick = { activeHourPicker = "morning" }
                )
                SettingsItem(
                    icon = Icons.Outlined.LightMode,
                    text = "Afternoon",
                    subtext = formatHour(state.afternoonHour, state.use24),
                    onClick = { activeHourPicker = "afternoon" }
                )
                SettingsItem(
                    icon = Icons.Outlined.WbTwilight,
                    text = "Evening",
                    subtext = formatHour(state.eveningHour, state.use24),
                    onClick = { activeHourPicker = "evening" }
                )
                SettingsItem(
                    icon = Icons.Outlined.Bedtime,
                    text = "Night",
                    subtext = formatHour(state.nightHour, state.use24),
                    onClick = { activeHourPicker = "night" }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(
                    horizontal = HorizontalSectionPadding,
                    vertical = SectionDividerPadding
                )
            )

            Section("Date & Time Formats") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HorizontalSectionPadding, vertical = CardVerticalPadding),
                    verticalArrangement = Arrangement.spacedBy(FormatsSpacing)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(ControlItemSpacing)) {
                        Text(
                            text = "Time Format",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        val timeFormatOptions = listOf(
                            TimeFormatMode.SYSTEM to "System",
                            TimeFormatMode.H12 to "12-Hour",
                            TimeFormatMode.H24 to "24-Hour"
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            timeFormatOptions.forEachIndexed { index, (mode, label) ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = timeFormatOptions.size
                                    ),
                                    onClick = { viewModel.setTimeFormatMode(mode) },
                                    selected = state.timeFormatMode == mode,
                                    label = {
                                        Text(label, style = MaterialTheme.typography.labelMedium)
                                    }
                                )
                            }
                        }
                        val timeSubtext = when (state.timeFormatMode) {
                            TimeFormatMode.SYSTEM -> if (state.is24HourResolved) {
                                "Currently: 24-Hour (from device)"
                            } else {
                                "Currently: 12-Hour (from device)"
                            }
                            TimeFormatMode.H12 -> "Currently: 12-Hour"
                            TimeFormatMode.H24 -> "Currently: 24-Hour"
                        }
                        Text(
                            text = timeSubtext,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(ControlItemSpacing)) {
                        Text(
                            text = "Date Format",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        val dateFormatOptions = listOf(
                            DateFormatMode.SYSTEM to "System",
                            DateFormatMode.MDY to "MM/DD/YYYY",
                            DateFormatMode.DMY to "DD/MM/YYYY"
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            dateFormatOptions.forEachIndexed { index, (mode, label) ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = dateFormatOptions.size
                                    ),
                                    onClick = { viewModel.setDateFormatMode(mode) },
                                    selected = state.dateFormatMode == mode,
                                    label = {
                                        Text(label, style = MaterialTheme.typography.labelMedium)
                                    }
                                )
                            }
                        }
                        val dateSubtext = when (state.dateFormatMode) {
                            DateFormatMode.SYSTEM -> if (state.isEuropeanDateResolved) {
                                "Currently: DD/MM/YYYY (from device)"
                            } else {
                                "Currently: MM/DD/YYYY (from device)"
                            }
                            DateFormatMode.MDY -> "Currently: MM/DD/YYYY"
                            DateFormatMode.DMY -> "Currently: DD/MM/YYYY"
                        }
                        Text(
                            text = dateSubtext,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(
                    horizontal = HorizontalSectionPadding,
                    vertical = SectionDividerPadding
                )
            )

            Section("Interactive Preview") {
                ComparisonDiffCard(
                    diffs = state.comparisonDiffs,
                    modifier = Modifier.padding(
                        horizontal = HorizontalSectionPadding,
                        vertical = CardVerticalPadding
                    )
                )
                LiveSandboxCard(
                    input = state.sandboxInput,
                    parsed = state.sandboxParsed,
                    onInputChange = viewModel::updateSandboxInput,
                    modifier = Modifier.padding(
                        horizontal = HorizontalSectionPadding,
                        vertical = CardVerticalPadding
                    )
                )
            }
        }
    }
}
