package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.text.InlineTextContent
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
import kotlin.math.min

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
    range: IntRange?,
    modifier: Modifier = Modifier,
    show: Boolean = true,
    color: Color = Color.Unspecified,
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

    val annotated = buildAnnotatedString {
        if (range == null) {
            append(text)
            return@buildAnnotatedString
        }

        append(text.substring(0, range.first))

        if (show) {
            withStyle(style = SpanStyle(color = primaryColor)) {
                append(text.substring(range))
            }
        }

        append(text.substring(min(range.last + 1, text.length)))
    }

    Text(
        annotated,
        modifier,
        color,
        fontSize,
        fontStyle,
        fontWeight,
        fontFamily,
        letterSpacing,
        textDecoration,
        textAlign,
        lineHeight,
        overflow,
        softWrap,
        maxLines,
        minLines,
        inlineContent,
        onTextLayout,
        style
    )
}
