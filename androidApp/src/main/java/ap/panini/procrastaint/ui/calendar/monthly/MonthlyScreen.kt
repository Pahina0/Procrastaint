import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ap.panini.procrastaint.ui.calendar.monthly.MonthlyViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MonthlyScreen(
    modifier: Modifier = Modifier,
    viewModel: MonthlyViewModel = koinViewModel(),
    onTodayClick: () -> Unit,
    onTitleChange: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        onTitleChange("Monthly View")
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Monthly Screen Placeholder")
    }
}