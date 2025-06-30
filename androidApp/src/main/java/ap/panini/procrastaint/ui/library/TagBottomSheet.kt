package ap.panini.procrastaint.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.ui.components.ColorPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagBottomSheet(
    onDismissRequest: () -> Unit,
    onSave: (TaskTag) -> Unit,
    state: BottomSheetTagState = rememberBottomSheetTagState(),
) {
    val tag = state.taskTag
    val color = remember(tag.color) {
        TaskTag.hexToRgb(tag.color).let {
            Color(it.first, it.second, it.third)
        }
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
                value = tag.title,
                onValueChange = state::updateTitle,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                label = { Text("Info") },
                value = tag.info,
                onValueChange = state::updateInfo,
                modifier = Modifier.fillMaxWidth()
            )

            ColorPicker(state.taskTag.color) {
                state.updateColor(it)
            }

            Button(onClick = { onSave(tag) }, modifier = Modifier.fillMaxWidth()) {
                Text("Save tag")
            }
        }
    }
}

class BottomSheetTagState(initialTag: TaskTag? = null) {
    var taskTag by mutableStateOf(initialTag ?: random())
        private set

    private fun random() = TaskTag("", "", TaskTag.generateRandomColor())

    fun randomReset() {
        taskTag = random()
    }

    fun setTag(tag: TaskTag) {
        this.taskTag = tag
    }


    fun updateTitle(title: String) {
        taskTag = taskTag.copy(title = title)
    }

    fun updateInfo(info: String) {
        taskTag = taskTag.copy(info = info)
    }

    fun updateColor(color: String) {
        taskTag = taskTag.copy(color = color)
    }
}

@Composable
fun rememberBottomSheetTagState(initialTag: TaskTag? = null): BottomSheetTagState {
    return remember { BottomSheetTagState(initialTag) }
}