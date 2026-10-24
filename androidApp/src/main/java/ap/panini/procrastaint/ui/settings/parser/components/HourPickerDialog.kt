package ap.panini.procrastaint.ui.settings.parser.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

private const val NoonHour = 12
private const val MaxHour = 23
private const val PadLength = 2

fun formatHour(hour: Int, use24: Boolean): String {
    val h = hour.coerceIn(0, MaxHour)
    return if (use24) {
        "${h.toString().padStart(PadLength, '0')}:00"
    } else {
        val period = if (h < NoonHour) "AM" else "PM"
        val displayHour = when (val rem = h % NoonHour) {
            0 -> NoonHour
            else -> rem
        }
        "$displayHour:00 $period"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourPickerDialog(
    title: String,
    initialHour: Int,
    use24: Boolean,
    onHourSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour.coerceIn(0, MaxHour),
        initialMinute = 0,
        is24Hour = use24
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(text = title) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onHourSelected(timePickerState.hour)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
