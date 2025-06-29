package ap.panini.procrastaint.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskTag
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagBottomSheet(
    state: LibraryViewModel.LibraryUiState,
    updateTitle: (String) -> Unit,
    updateColor: (String) -> Unit,
    updateInfo: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onSave: (TaskTag) -> Unit
) {
    val controller = rememberColorPickerController()

    LaunchedEffect(state.tag.color) {
        println(state.tag.color)
//        val color = TaskTag.hexToRgb(state.tag.color).let { Color(it.first, it.second, it.third) }
//        controller.selectByColor(color, fromUser = true)
    }


    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = { onDismissRequest() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Create a new tag")

            OutlinedTextField(
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Tag,
                        contentDescription = null,
                        tint = TaskTag.hexToRgb(state.tag.color)
                            .let { Color(it.first, it.second, it.third) }
                    )
                },
                label = { Text("Title") },
                value = state.tag.title,
                onValueChange = updateTitle,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                label = { Text("Info") },
                value = state.tag.info,
                onValueChange = updateInfo,
                modifier = Modifier.fillMaxWidth()
            )

            HsvColorPicker(
                initialColor = TaskTag.hexToRgb(state.tag.color)
                    .let { Color(it.first, it.second, it.third) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                controller = controller,
                onColorChanged = {
                    updateColor(it.color.let {
                        TaskTag.rgbToHex(
                            it.red.toInt(),
                            it.green.toInt(),
                            it.blue.toInt()
                        )
                    })
                }
            )
        }

    }
}