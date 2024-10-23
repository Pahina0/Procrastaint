package ap.panini.procrastaint.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

class SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Settings"
            val icon = rememberVectorPainter(Icons.Outlined.Settings)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        SettingsScreen()
    }
}

@Composable
fun SettingsScreen() {
    // Use Column to stack buttons and other settings
    Column {
        Text(text = "Settings")

        // Add buttons for different settings
        Button(
            onClick = { /* Handle theme change */ },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Change Theme")
        }

    }
}

@Preview
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen()
}