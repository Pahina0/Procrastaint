package ap.panini.procrastaint.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import ap.panini.procrastaint.ui.components.ColorPicker
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagBottomSheet(
    tag: TaskTag,
    onDismissRequest: () -> Unit,
    onSave: (TaskTag) -> Unit
) {

    // Internal state
    var internalTag by remember(tag) { mutableStateOf(tag) }

    val color by remember(internalTag) {
        mutableStateOf(TaskTag.hexToRgb(internalTag.color).let {
            Color(it.first, it.second, it.third)
        }
        )
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismissRequest
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
                        tint = color
                    )
                },
                label = { Text("Title") },
                value = internalTag.title,
                onValueChange = { internalTag = internalTag.copy(title = it) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                label = { Text("Info") },
                value = internalTag.info,
                onValueChange = { internalTag = internalTag.copy(info = it) },
                modifier = Modifier.fillMaxWidth()
            )

            ColorPicker(tag.color) {
                internalTag = internalTag.copy(color = it)
            }
        }
    }
}
