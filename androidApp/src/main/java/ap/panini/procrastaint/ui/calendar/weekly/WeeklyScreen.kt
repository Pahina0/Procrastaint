package ap.panini.procrastaint.ui.calendar.weekly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeeklyScreen(
    modifier: Modifier = Modifier,
    viewModel: WeeklyViewModel = koinViewModel(),
    onTodayClick: () -> Unit,
    onTitleChange: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        onTitleChange("Weekly View")
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Weekly Screen Placeholder")
    }
}