package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskTag
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@Composable
fun ColorPicker(
    color: String,
    modifier: Modifier = Modifier,
    onColorChange: (String) -> Unit
) {
    val controller = rememberColorPickerController()

    val parsedColor = try {
        TaskTag.hexToRgb(color).let { Color(it.first, it.second, it.third) }
    } catch (_: Exception) {
        Color.Gray
    }

    var hexTextColor by remember { mutableStateOf(color) }

    LaunchedEffect(color) {
        hexTextColor = color
        controller.selectByColor(parsedColor, fromUser = false)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HsvColorPicker(
            initialColor = parsedColor,
            modifier = Modifier.size(100.dp),
            controller = controller,
            onColorChanged = {
                val hex = TaskTag.rgbToHex(
                    (it.color.red * 255).toInt(),
                    (it.color.green * 255).toInt(),
                    (it.color.blue * 255).toInt()
                )
                hexTextColor = hex
                onColorChange(hex)
            }
        )

        OutlinedTextField(
            label = { Text("Hex") },
            value = hexTextColor,
            onValueChange = {
                hexTextColor = it
                try {
                    TaskTag.hexToRgb(it)
                    onColorChange(it)
                } catch (_: Exception) {
                    // do nothing or show error
                }
            },
            isError = try {
                TaskTag.hexToRgb(hexTextColor)
                false
            } catch (_: Exception) {
                true
            },
            modifier = Modifier.weight(1f)
        )
    }
}
