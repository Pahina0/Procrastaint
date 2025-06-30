package ap.panini.procrastaint.ui.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.ui.components.ParsedText
import ap.panini.procrastaint.ui.components.TimeChips
import ap.panini.procrastaint.util.Parsed
import ap.panini.procrastaint.util.Time

@Composable
fun ParserExample(parsed: Parsed?, text: String, modifier: Modifier = Modifier) {
    val timeOptions = mapOf(
        null to "Auto",
        Time.YEAR to "Year",
        Time.MONTH to "Month",
        Time.WEEK to "Week",
        Time.DAY to "Day",
        Time.HOUR to "Hour",
        Time.MINUTE to "Minute"
    )

    val parsedTime = parsed?.times?.getOrNull(0)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        ParsedText(
            text = text,
            parsed,
            0,
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

                ParsedText(
                    text = text,
                    parsed,
                    0,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    show = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
