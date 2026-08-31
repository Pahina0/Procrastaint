package ap.panini.procrastaint.ui.onboarding.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Try it yourself!\nType a task like: \"Brush teeth at 9pm every day\"",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        OutlinedTextField(
            value = text,
            isError = text.isNotBlank() && !success,
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
            label = { Text("Task description") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ParserExample(parsed, text)

        if (success) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Great job! Time was detected automatically.",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
