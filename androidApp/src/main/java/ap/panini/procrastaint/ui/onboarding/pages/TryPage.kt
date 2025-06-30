package ap.panini.procrastaint.ui.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.hour
import org.koin.compose.koinInject

@Composable
fun TryPage(modifier: Modifier = Modifier) {
    val parser: Parser = koinInject()
    var text by remember { mutableStateOf("") }
    var parsed by remember { mutableStateOf<Parsed?>(null) }

    var success by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            "Try it yourself!\nMake a task that happens at 9pm every day.",
            textAlign = TextAlign.Center
        )

        HorizontalDivider(modifier = Modifier.padding(15.dp))

        OutlinedTextField(
            text,
            isError = !success,
            onValueChange = {
                parsed = parser.parse(it)

                val parsedTime = parsed?.times?.firstOrNull()

                success = parsedTime != null &&
                    parsedTime.repeatOften == 1 &&
                    parsedTime.repeatTag == Time.DAY &&
                    parsedTime.startTimes.size == 1 &&
                    parsedTime.startTimes.first().hour() == 21

                text = it
            },
            placeholder = {
                Text("Brush teeth at 9pm everyday")
            },
            label = { Text("Make your own task!") },
        )

        ParserExample(parsed, text)

        if (success) {
            Text("Congratulations! You got it!")
        }
    }
}
