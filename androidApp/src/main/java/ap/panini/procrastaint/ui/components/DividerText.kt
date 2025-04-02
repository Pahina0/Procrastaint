package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DividerText(
    text: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            thickness = DividerDefaults.Thickness + if (highlight) 1.dp else 0.dp,
            color = if (highlight) MaterialTheme.colorScheme.primary else DividerDefaults.color
        )

        Text(text, fontWeight = if (highlight) FontWeight.Bold else null)

        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            thickness = DividerDefaults.Thickness + if (highlight) 1.dp else 0.dp,
            color = if (highlight) MaterialTheme.colorScheme.primary else DividerDefaults.color
        )
    }
}

@Preview
@Composable
private fun TimeViewPreview() {
    DividerText("2:00", modifier = Modifier.fillMaxWidth())
}
