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
    onColorChange: (String) -> Unit
) {
    val controller = rememberColorPickerController()

    var hexTextColor by remember(color) {
        mutableStateOf(color)
    }

    val parsedColor: Color? = try {
        TaskTag.hexToRgb(hexTextColor)
            .let { Color(it.first, it.second, it.third) }
    } catch (_: Exception) {
        null
    }

    val color = parsedColor ?: try {
        TaskTag.hexToRgb(color)
            .let { Color(it.first, it.second, it.third) }
    } catch (_: Exception) {
        Color.Gray
    }

    LaunchedEffect(hexTextColor) {
        parsedColor?.let {
            controller.selectByColor(
                TaskTag.hexToRgb(hexTextColor)
                    .let { Color(it.first, it.second, it.third) },
                fromUser = false
            )
            onColorChange(hexTextColor)
        }
    }


    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HsvColorPicker(
            initialColor = color,
            modifier = Modifier.size(100.dp),
            controller = controller,
            onColorChanged = {
                val hex = TaskTag.rgbToHex(
                    (it.color.red * 255).toInt(),
                    (it.color.green * 255).toInt(),
                    (it.color.blue * 255).toInt()
                )
                hexTextColor = hex // update hex text field
            }
        )

        OutlinedTextField(
            label = { Text("Hex") },
            value = hexTextColor,
            onValueChange = { hexTextColor = it },
            isError = parsedColor == null,
            modifier = Modifier.weight(1f)
        )
    }
}