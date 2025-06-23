package ap.panini.procrastaint.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingsItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    onClick: (() -> Unit)? = null,
    trailingItem: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(text)
            subtext?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        trailingItem?.invoke()
    }
}

@Preview
@Composable
private fun SettingsTextPreview() {
    SettingsItem(
        Icons.Outlined.Cake,
        "Major text that is super duppper long and long and goes on forever",
        subtext = "minor text that is super duppper long and long and goes on forever",
    ) {
        Switch(checked = false, onCheckedChange = {})
    }
}

@Preview
@Composable
private fun SettingsTextPreviewEmpty() {
    SettingsItem(
        Icons.Outlined.Cake,
        "Major text that is super duppper long and long and goes on forever",
    ) {
        Switch(checked = false, onCheckedChange = {})
    }
}
