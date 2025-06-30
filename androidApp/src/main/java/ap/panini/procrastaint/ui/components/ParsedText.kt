package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.util.Parsed
import ap.panini.procrastaint.util.Time

/**
 * Parsed text a text with the highlighted range
 *
 * @param text
 * @param range
 * @param modifier
 * @param show if true, it shows the extracted text as primary color or else it removes the extracted text
 * @param color
 * @param fontSize
 * @param fontStyle
 * @param fontWeight
 * @param fontFamily
 * @param letterSpacing
 * @param textDecoration
 * @param textAlign
 * @param lineHeight
 * @param overflow
 * @param softWrap
 * @param maxLines
 * @param minLines
 * @param inlineContent
 * @param onTextLayout
 * @param style
 * @receiver
 */
@Composable
fun ParsedText(
    text: String,
    parsed: Parsed?,
    selectedTime: Int,
    modifier: Modifier = Modifier,
    show: Boolean = true,
    color: Color = Color.Unspecified,
    autoSize: TextAutoSize? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val extracted = mutableListOf<Extracted>()

    parsed?.let {
        val time = it.times.getOrNull(selectedTime)?.extractedRange
        if (time != null) {
            extracted += Extracted.Time(time)
        }

        extracted += it.tags.map { Extracted.Tag(it.tag, it.extractedRange) }
    }

    extracted.sortBy { it.range.first }

    val annotated = buildAnnotatedString {
        if (parsed == null || extracted.isEmpty()) {
            append(text)
            return@buildAnnotatedString
        }

        var currentIndex = 0

        for (item in extracted) {
            val range = item.range
            if (currentIndex < range.first) {
                append(text.substring(currentIndex, range.first))
            }

            if (show) {
                withStyle(
                    style = SpanStyle(
                        color = when (item) {
                            is Extracted.Tag -> item.tag.toRgbOrNull()?.let {
                                Color(it.first, it.second, it.third)
                            } ?: tertiaryColor

                            else -> primaryColor
                        }
                    )
                ) {
                    append(text.substring(range))
                }
            }

            currentIndex = range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }

    Text(
        annotated,
        modifier = modifier,
        color = color,
        autoSize = autoSize,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
        style = style
    )
}

private sealed class Extracted(val range: IntRange) {
    class Time(range: IntRange) : Extracted(range)
    class Tag(val tag: TaskTag, range: IntRange) : Extracted(range)
}
