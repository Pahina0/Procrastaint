package ap.panini.procrastaint.ui.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.ui.onboarding.components.ParserExample
import ap.panini.procrastaint.util.Parsed
import ap.panini.procrastaint.util.Parser
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.hour

@Composable
fun TryPage(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    var parsed by remember { mutableStateOf<Parsed?>(null) }

    val successColor = MaterialTheme.colorScheme.tertiary
    var outlineColor by remember { mutableStateOf(Color.Unspecified) }

    LaunchedEffect(text) {
    }

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
            onValueChange = {
                parsed = Parser().parse(it).firstOrNull()

                outlineColor = if (parsed != null &&
                    parsed!!.repeatOften == 1 &&
                    parsed!!.repeatTag == Time.DAY &&
                    parsed!!.startTimes.size == 1 &&
                    parsed!!.startTimes.first().hour() == 21
                ) {
                    successColor
                } else {
                    Color.Unspecified
                }

                text = it
            },
            placeholder = {
                Text("Brush teeth at 9pm everyday")
            },
            label = { Text("Make your own task!") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = outlineColor,
                unfocusedBorderColor = outlineColor
            )
        )

        ParserExample(parsed, text)
    }
}
