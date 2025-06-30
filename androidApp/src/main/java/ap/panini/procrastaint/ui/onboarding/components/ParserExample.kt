package ap.panini.procrastaint.ui.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.ui.components.TimeChips
import ap.panini.procrastaint.ui.components.rememberParsedText
import ap.panini.procrastaint.util.Parsed

@Composable
fun ParserExample(parsed: Parsed?, text: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            text = rememberParsedText(
                text = text,
                parsed,
                0,
            ),
            textAlign = TextAlign.Center
        )

        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimeChips(parsed?.times?.getOrNull(0))

                Spacer(modifier = Modifier.padding(30.dp))

                Text(
                    text = rememberParsedText(
                        text = text,
                        parsed,
                        0,
                        show = false
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth()
                )

            }
        }
    }
}
