package ap.panini.procrastaint.ui.calendar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.ui.calendar.CalendarDisplayMode

@Composable
fun CalendarFilterBottomSheet(
    displayMode: CalendarDisplayMode,
    onDisplayModeChange: (CalendarDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Display", "Filter")

    Column(modifier = modifier) {
        SecondaryTabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        when (tabIndex) {
            0 -> DisplayTabContent(displayMode, onDisplayModeChange)
            1 -> FilterTabContent()
        }
    }
}

@Composable
private fun DisplayTabContent(
    displayMode: CalendarDisplayMode,
    onDisplayModeChange: (CalendarDisplayMode) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Organization",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        CalendarDisplayMode.entries.forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (displayMode == mode),
                        onClick = { onDisplayModeChange(mode) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = (displayMode == mode),
                    onClick = null // click is handled by parent
                )
                Text(
                    text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterTabContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Filter options will be here.")
    }
}
