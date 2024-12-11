package ap.panini.procrastaint.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.ui.theme.scrimLight
import ap.panini.procrastaint.ui.theme.secondaryLight

@Composable
fun DayView(
    day: Int,
    toDos: List<String>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit // Function to go back to the Month view
){
    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        Text(
            text = "Day $day",
            style = MaterialTheme.typography.headlineMedium,
            color = scrimLight
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display the to-dos for the day
        toDos.forEach { todo ->
            Text(
                text = "- $todo",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Button to go back to the Month view
        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Back to Month View")
        }

    }
}


@Composable
fun DayItem(day: String, isToday: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isToday) Color.Blue else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$day",
            color = if (isToday) scrimLight else secondaryLight
        )
    }
}
