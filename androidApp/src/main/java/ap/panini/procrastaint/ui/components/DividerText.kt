package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DividerText(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier
            .weight(1f)
            .padding(horizontal = 10.dp))

        Text(text)

        HorizontalDivider(modifier = Modifier
            .weight(1f)
            .padding(horizontal = 10.dp))
    }
}

@Preview
@Composable
private fun TimeViewPreview() {
    DividerText("2:00", modifier = Modifier.fillMaxWidth())
}
