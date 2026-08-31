package ap.panini.procrastaint.ui.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.resources.Res
import ap.panini.procrastaint.resources.daily
import ap.panini.procrastaint.resources.device_calendar
import ap.panini.procrastaint.resources.filter_calendar
import ap.panini.procrastaint.resources.google_calendar
import ap.panini.procrastaint.resources.monthly
import ap.panini.procrastaint.resources.procrastaint_tasks
import ap.panini.procrastaint.resources.show_completed
import ap.panini.procrastaint.resources.show_incomplete
import ap.panini.procrastaint.resources.view_mode
import ap.panini.procrastaint.resources.weekly
import ap.panini.procrastaint.ui.calendar.CalendarDisplayMode
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CalendarFilterBottomSheet(
    displayMode: CalendarDisplayMode,
    onDisplayModeChange: (CalendarDisplayMode) -> Unit,
    showProcrastaintTasks: Boolean,
    showDeviceCalendar: Boolean,
    showGoogleCalendar: Boolean,
    showCompleted: Boolean,
    showIncomplete: Boolean,
    onToggleProcrastaintTasks: (Boolean) -> Unit,
    onToggleDeviceCalendar: (Boolean) -> Unit,
    onToggleGoogleCalendar: (Boolean) -> Unit,
    setShowCompleted: (Boolean) -> Unit,
    setShowIncomplete: (Boolean) -> Unit,
    hasDeviceCalendarPermission: Boolean = true,
    isGoogleLoggedIn: Boolean = false,
    onRequestDeviceCalendarPermission: () -> Unit = {},
    onConnectGoogle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.filter_calendar),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        // View Mode Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(Res.string.view_mode),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CalendarDisplayMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = CalendarDisplayMode.entries.size
                        ),
                        onClick = { onDisplayModeChange(mode) },
                        selected = displayMode == mode,
                        label = {
                            Text(
                                text = when (mode) {
                                    CalendarDisplayMode.DAILY -> stringResource(Res.string.daily)
                                    CalendarDisplayMode.WEEKLY -> stringResource(Res.string.weekly)
                                    CalendarDisplayMode.MONTHLY -> stringResource(Res.string.monthly)
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Sources Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Visible Calendars & Sources",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = showProcrastaintTasks,
                    onClick = { onToggleProcrastaintTasks(!showProcrastaintTasks) },
                    label = { Text(stringResource(Res.string.procrastaint_tasks)) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (showProcrastaintTasks) Icons.Outlined.Check else Icons.Outlined.TaskAlt,
                            contentDescription = null
                        )
                    }
                )

                FilterChip(
                    selected = showDeviceCalendar && hasDeviceCalendarPermission,
                    onClick = {
                        if (!hasDeviceCalendarPermission) {
                            onRequestDeviceCalendarPermission()
                        } else {
                            onToggleDeviceCalendar(!showDeviceCalendar)
                        }
                    },
                    label = {
                        Text(stringResource(Res.string.device_calendar))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when {
                                !hasDeviceCalendarPermission -> Icons.Outlined.Lock
                                showDeviceCalendar -> Icons.Outlined.Check
                                else -> Icons.Outlined.CalendarMonth
                            },
                            contentDescription = null
                        )
                    },
                    colors = if (!hasDeviceCalendarPermission) {
                        FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    } else {
                        FilterChipDefaults.filterChipColors()
                    }
                )

                FilterChip(
                    selected = showGoogleCalendar && isGoogleLoggedIn,
                    onClick = {
                        if (!isGoogleLoggedIn) {
                            onConnectGoogle()
                        } else {
                            onToggleGoogleCalendar(!showGoogleCalendar)
                        }
                    },
                    label = {
                        Text(stringResource(Res.string.google_calendar))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when {
                                !isGoogleLoggedIn -> Icons.Outlined.AccountCircle
                                showGoogleCalendar -> Icons.Outlined.Check
                                else -> Icons.Outlined.Public
                            },
                            contentDescription = null
                        )
                    },
                    colors = if (!isGoogleLoggedIn) {
                        FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    } else {
                        FilterChipDefaults.filterChipColors()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Task Status Filters Section
        if (showProcrastaintTasks) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Task Status",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = showIncomplete,
                        onClick = { setShowIncomplete(!showIncomplete) },
                        label = { Text(stringResource(Res.string.show_incomplete)) },
                        leadingIcon = if (showIncomplete) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null
                                )
                            }
                        } else {
                            null
                        }
                    )

                    FilterChip(
                        selected = showCompleted,
                        onClick = { setShowCompleted(!showCompleted) },
                        label = { Text(stringResource(Res.string.show_completed)) },
                        leadingIcon = if (showCompleted) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}
