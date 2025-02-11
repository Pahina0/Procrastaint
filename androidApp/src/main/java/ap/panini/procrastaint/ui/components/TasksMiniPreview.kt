package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time

@Composable
fun TasksMiniPreview(date: Long, tasks: List<TaskSingle>, modifier: Modifier = Modifier) {
    val states = tasks.joinToString("") { it.completed?.let { "O" } ?: "X" }

    Card(
        modifier = modifier.width(100.dp).height(70.dp),
        shape = RoundedCornerShape(50.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(10.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly

        ) {
            Text(date.formatMilliseconds(setOf(Time.DAY)))
            Text(states, overflow = TextOverflow.Ellipsis, maxLines = 1)
        }
    }
}