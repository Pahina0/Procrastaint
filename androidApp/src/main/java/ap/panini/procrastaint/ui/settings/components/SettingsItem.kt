package ap.panini.procrastaint.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SettingsItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    onClick: (() -> Unit)? = null,
    trailingItem: @Composable () -> Unit = {},
) {
    ListItem(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        ),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        headlineContent = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = subtext?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            trailingItem()
        }
    )
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
