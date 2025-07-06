package ap.panini.procrastaint.ui.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskTag

@Composable
fun TagItem(
    tag: TaskTag,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Tag,
                contentDescription = null,
                tint = tag.toRgb().let { c -> Color(c.first, c.second, c.third) }
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    itemVerticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        tag.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        "#${tag.title}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                if (tag.info.isNotBlank()) {
                    Text(tag.info, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}