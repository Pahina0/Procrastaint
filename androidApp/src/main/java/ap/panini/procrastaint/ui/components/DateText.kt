package ap.panini.procrastaint.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time

@Composable
fun DateText(
    date: Long?
) {
    Text(
        date?.formatMilliseconds(setOf(Time.DAY, Time.MONTH)) ?: "Unknown",
        style = MaterialTheme.typography.headlineLarge    )
}