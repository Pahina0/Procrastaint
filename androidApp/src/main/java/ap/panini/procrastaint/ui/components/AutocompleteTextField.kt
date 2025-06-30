package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AutoCompleteTextField(
    suggestions: List<T>,
    dropdownContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    suggestionToString: T.() -> String = { toString() },
    label: @Composable (() -> Unit)? = null,
    onCurrentWordChanged: (String) -> Unit = {},
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var expanded by remember { mutableStateOf(false) }

    val text = textFieldValue.text
    val cursorPos = textFieldValue.selection.start.coerceIn(0, text.length)

    val wordAtCursor = remember(text, cursorPos) {
        val before = text.substring(0, cursorPos)
        val after = text.substring(cursorPos)

        val start =
            before.lastIndexOfAny(charArrayOf(' ', '\n')).let { if (it == -1) 0 else it + 1 }
        val end = after.indexOfAny(charArrayOf(' ', '\n'))
            .let { if (it == -1) text.length else cursorPos + it }

        text.substring(start, end.coerceAtLeast(start))
    }

    LaunchedEffect(textFieldValue) {
        onValueChange(textFieldValue.text)
        expanded = suggestions.isNotEmpty()
    }

    LaunchedEffect(wordAtCursor) {
        onCurrentWordChanged(wordAtCursor)
        expanded = suggestions.isNotEmpty()
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onValueChange(it.text)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon ?: {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource ?: remember { MutableInteractionSource() },
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            shape = shape,
            colors = colors
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { dropdownContent(suggestion) },
                    onClick = {
                        val suggestionString = suggestion.suggestionToString()
                        val beforeCursor = text.substring(0, cursorPos)
                        val afterCursor = text.substring(cursorPos)

                        // Find bounds of hovered word
                        val start = beforeCursor.lastIndexOfAny(charArrayOf(' ', '\n'))
                            .let { if (it == -1) 0 else it + 1 }

                        val end = afterCursor.indexOfAny(charArrayOf(' ', '\n'))
                            .let { if (it == -1) text.length else cursorPos + it }

                        // Compose new text
                        val newText = buildString {
                            append(text.substring(0, start))
                            append(suggestionString)
                            if (end < text.length) {
                                append(text.substring(end))
                            }
                        }

                        val newCursorPos =
                            (start + suggestionString.length).coerceAtMost(newText.length)

                        textFieldValue = TextFieldValue(
                            text = newText,
                            selection = TextRange(newCursorPos)
                        )
                        onValueChange(textFieldValue.text)
                        expanded = false
                    }
                )
            }
        }
    }
}
