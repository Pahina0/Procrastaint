package ap.panini.procrastaint.ui.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.ui.onboarding.components.ParserExample
import ap.panini.procrastaint.util.Parsed
import ap.panini.procrastaint.util.Parser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.compose.koinInject

@Composable
fun DemoPage(modifier: Modifier = Modifier) {
    val parser: Parser = koinInject()
    var text by remember { mutableStateOf("") }
    var parsed by remember { mutableStateOf<Parsed?>(null) }
    val sentences = listOf(
        "Write a task at 8 everyday starting on friday for 3 months",
        "Go on a jog every mon, thurs, sun at 6am",
        "Prepare for a party in a week",
        "Meditate jun 8th 9pm",
        "Start cooking at 11am for 5 weeks every tuesday"
    )

    LaunchedEffect(Unit) {
        simulateTyping(sentences)
            .collect { typedText ->
                parsed = parser.parse(typedText)
                text = typedText
            }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            "Enter a task with a time and we'll detect it automatically",
            textAlign = TextAlign.Center
        )

        HorizontalDivider(modifier = Modifier.padding(15.dp))

        ParserExample(parsed, text)
    }
}

private fun simulateTyping(
    sentences: List<String>,
    delayMs: Long = 100,
    pause: Long = 5000
): Flow<String> = flow {
    while (true) {
        val sentence = sentences.random()
        for (i in 1..sentence.length) {
            emit(sentence.substring(0, i))
            delay(delayMs)
        }

        delay(pause)
    }
}
