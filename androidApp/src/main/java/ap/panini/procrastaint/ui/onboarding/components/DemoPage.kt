package ap.panini.procrastaint.ui.onboarding.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.util.Parsed
import ap.panini.procrastaint.util.Parser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Composable
fun DemoPage(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(buildAnnotatedString { }) }
    var parsed by remember { mutableStateOf<Parsed?>(null) }
    val sentence = "Do something at 8pm everyday"

    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(sentence) {
        simulateTyping(sentence)
            .collect { typedText ->
                val parser = Parser()
                parsed = parser.parse(typedText).firstOrNull()
                text = buildAnnotatedString {
                    val range = parsed?.extractedRange

                    if (range == null) {
                        append(typedText)
                        return@buildAnnotatedString
                    }

                    append(typedText.substring(0, range.first))
                    withStyle(style = SpanStyle(color = primaryColor)) {
                        append(typedText.substring(range))
                    }

                    append(typedText.substring(range.last + 1))
                }
            }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            "Type your task with the time and the time will automatically get extracted!",
            textAlign = TextAlign.Center
        )

        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

fun simulateTyping(sentence: String, delayMs: Long = 100): Flow<String> = flow {
    while (true) {
        for (i in 1..sentence.length) {
            emit(sentence.substring(0, i))
            delay(delayMs)
        }

        delay(5000L)
    }
}