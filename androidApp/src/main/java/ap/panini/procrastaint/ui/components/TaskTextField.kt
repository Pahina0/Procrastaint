package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTextField(
    suggestions: List<TaskTag>,
    modifier: Modifier = Modifier,
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
    }

    LaunchedEffect(wordAtCursor) {
        onCurrentWordChanged(wordAtCursor)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onValueChange(it.text)
            },
            modifier = Modifier.fillMaxWidth(),
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
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

        if (suggestions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(suggestions) { suggestion ->
                    val onClick: () -> Unit =
                        {
                            val suggestionString = suggestion.generateTag()
                            val beforeCursor = text.substring(0, cursorPos)
                            val afterCursor = text.substring(cursorPos)

                            val start = beforeCursor.lastIndexOfAny(charArrayOf(' ', '\n'))
                                .let { if (it == -1) 0 else it + 1 }

                            val end = afterCursor.indexOfAny(charArrayOf(' ', '\n'))
                                .let { if (it == -1) text.length else cursorPos + it }

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
                        }

                    SuggestionChip(icon = {
                        Icon(
                            Icons.Outlined.Tag,
                            contentDescription = null,
                            tint = suggestion.toRgb().let { Color(it.first, it.second, it.third) }
                        )
                    }, label = { Text(suggestion.title) }, onClick = onClick)
                }
            }
        }
    }
}
