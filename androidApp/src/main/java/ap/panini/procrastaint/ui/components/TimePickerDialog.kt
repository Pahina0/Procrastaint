package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.KeyboardAlt
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// from https://github.com/segunfrancis/Date-and-Time-Pickers/blob/master/app/src/main/java/com/segunfrancis/dateandtimepickers/ui/TimePickerDialog.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "Select Time",
    content: @Composable (DisplayMode) -> Unit,
) {
    var displayModeState by remember { mutableStateOf(DisplayMode.Input) }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content(displayModeState)
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    DisplayModeToggleButton(
                        displayModeState = displayModeState,
                        changeDisplayMode = {
                            displayModeState = it
                        }
                    )

                    Spacer(modifier = Modifier.weight(1F))
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                    TextButton(onClick = onConfirm) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisplayModeToggleButton(
    displayModeState: DisplayMode,
    changeDisplayMode: (DisplayMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (displayModeState) {
        DisplayMode.Picker -> IconButton(
            modifier = modifier,
            onClick = { changeDisplayMode(DisplayMode.Input) },
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "time_picker_button_select_input_mode"
            )
        }

        DisplayMode.Input -> IconButton(
            modifier = modifier,
            onClick = { changeDisplayMode(DisplayMode.Picker) },
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardAlt,
                contentDescription = "time_picker_button_select_picker_mode"
            )
        }
    }
}
