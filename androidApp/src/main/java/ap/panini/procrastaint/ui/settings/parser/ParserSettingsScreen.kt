package ap.panini.procrastaint.ui.settings.parser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.ui.settings.components.Section
import ap.panini.procrastaint.ui.settings.components.SettingsItem
import ap.panini.procrastaint.ui.settings.parser.components.HourPickerDialog
import ap.panini.procrastaint.ui.settings.parser.components.LiveSandboxCard
import ap.panini.procrastaint.ui.settings.parser.components.formatHour
import org.koin.androidx.compose.koinViewModel

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
                title = { Text("Date & Time Parsing") },
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
                .padding(bottom = 24.dp)
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Section("Date & Time Notation") {
                SettingsItem(
                    icon = Icons.Outlined.CalendarMonth,
                    text = "European Date Format",
                    subtext = "Parse numeric dates as DD/MM/YYYY (e.g. 03/04 = April 3rd)",
                    trailingItem = {
                        Switch(
                            checked = state.useEuropeanDate,
                            onCheckedChange = viewModel::setUseEuropeanDate
                        )
                    }
                )
                SettingsItem(
                    icon = Icons.Outlined.Schedule,
                    text = "24-Hour Time Format",
                    subtext = "Assume 24-hour clock for ambiguous single numbers (e.g. 'at 14')",
                    trailingItem = {
                        Switch(
                            checked = state.use24,
                            onCheckedChange = viewModel::setUse24
                        )
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Section("Interactive Preview") {
                LiveSandboxCard(
                    input = state.sandboxInput,
                    parsed = state.sandboxParsed,
                    onInputChange = viewModel::updateSandboxInput,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
