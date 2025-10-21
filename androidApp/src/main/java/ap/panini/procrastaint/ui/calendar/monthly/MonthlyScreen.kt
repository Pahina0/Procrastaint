package ap.panini.procrastaint.ui.calendar.monthly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel

@Composable
fun MonthlyScreen(
    modifier: Modifier = Modifier,
    viewModel: MonthlyViewModel = koinViewModel(),
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Monthly Screen Placeholder")
    }
}