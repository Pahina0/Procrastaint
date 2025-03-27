package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time

@Composable
fun TasksMiniPreview(
    date: Long,
    tasks: List<TaskSingle>,
    modifier: Modifier = Modifier,
    currentDateColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit = {},
) {
    Card(
        onClick = onClick,
        modifier = modifier
//            .width(100.dp)
            .height(50.dp),
        shape = RoundedCornerShape(50.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)

        ) {
            Text(
                date.formatMilliseconds(setOf(Time.DAY), useAbbreviated = true),
                color = currentDateColor,
            )
            val remaining = tasks.filter { it.completed == null }.size

            if (remaining > 0) {
                Badge {
                    Text(
                        remaining.toString(),
                    )
                }
            }
//            Row {
//                Text(
//                    tasks.filter { it.completed == null }.size.toString(),
//                    color = currentDateColor
//                )
//            }
        }
    }
}
