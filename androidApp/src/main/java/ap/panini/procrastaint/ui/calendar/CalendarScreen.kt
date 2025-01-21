package ap.panini.procrastaint.ui.calendar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>(
    start = true
)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier
) {
    Text(
        text = "Calendar",
        modifier = modifier
    )
}
