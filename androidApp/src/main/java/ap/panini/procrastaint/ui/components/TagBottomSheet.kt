package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagBottomSheet(
    onDismissRequest: () -> Unit,
    onSave: (TaskTag) -> Unit,
    isValidTag: (TaskTag) -> Boolean,
    state: BottomSheetTagState = rememberBottomSheetTagState(),
) {
    val tag = state.taskTag
    val color = remember(tag.color) {
        tag.toRgb().let {
            Color(it.first, it.second, it.third)
        }
    }

    val validTag by remember(tag.title) {
        mutableStateOf(
            isValidTag(tag) && tag.title.isNotBlank()
        )
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (tag.tagId == 0L) "New Tag" else "Edit Tag",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )

            OutlinedTextField(
                label = { Text("Display Title") },
                placeholder = { Text("e.g. Work, Home, Study") },
                value = tag.displayTitle,
                onValueChange = state::updateDisplayTitle,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Tag,
                        contentDescription = null,
                        tint = color
                    )
                },
                isError = !validTag && tag.title.isNotBlank(),
                supportingText = if (!validTag && tag.title.isNotBlank()) {
                    { Text("Identifier must be unique and non-empty") }
                } else null,
                label = { Text("Tag Identifier (#slug)") },
                value = tag.title,
                onValueChange = {
                    state.updateTitle(it)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                label = { Text("Description / Notes (optional)") },
                value = tag.info,
                onValueChange = state::updateInfo,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            ColorPicker(state.taskTag.color, onColorChange = { state.updateColor(it) })

            Button(
                enabled = validTag,
                onClick = { onSave(tag) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Save Tag",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

class BottomSheetTagState(initialTag: TaskTag? = null) {
    var taskTag by mutableStateOf(initialTag ?: random())
        private set

    private fun random() = TaskTag("", "", "", TaskTag.generateRandomColor())

    fun randomReset() {
        taskTag = random()
    }

    fun setTag(tag: TaskTag) {
        this.taskTag = tag
    }

    fun updateDisplayTitle(title: String) {
        taskTag = taskTag.copy(
            displayTitle = title,
            title = title.replace(' ', '-')
        )
    }

    fun updateTitle(title: String) {
        taskTag = taskTag.copy(
            title = title.replace(' ', '-'),
        )
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
