package ap.panini.procrastaint.ui.agenda.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.CalendarSource
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time

private const val PillBackgroundAlpha = 0.08f
private const val PillBorderAlpha = 0.25f
private const val HairlineStartAlpha = 0.35f
private const val LocationIconAlpha = 0.7f
private const val SeparatorAlpha = 0.4f
private val PillCornerRadius = 16.dp
private val PillBorderWidth = 1.dp
private val HairlineHeight = 1.dp
private val MinHairlineWidth = 16.dp
private val LocationIconSize = 11.dp

@Composable
fun AgendaScheduleDivider(
    event: ExternalCalendarEvent,
    modifier: Modifier = Modifier,
    onEventClick: ((ExternalCalendarEvent) -> Unit)? = null
) {
    val eventColor = event.color
    val accentColor = if (eventColor != null && eventColor != 0) {
        Color(eventColor)
    } else {
        if (event.source == CalendarSource.GOOGLE) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.secondary
        }
    }

    val timeString = if (event.isAllDay) {
        "All Day"
    } else {
        val start = event.startTime.formatMilliseconds(setOf(Time.HOUR, Time.MINUTE), smart = false)
        val end = event.endTime.formatMilliseconds(setOf(Time.HOUR, Time.MINUTE), smart = false)
        "$start - $end"
    }

    val location = event.location

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Left gradient hairline rule
        Box(
            modifier = Modifier
                .widthIn(min = MinHairlineWidth)
                .weight(1f)
                .height(HairlineHeight)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, accentColor.copy(alpha = HairlineStartAlpha))
                    )
                )
        )

        // Center pill badge
        Surface(
            shape = RoundedCornerShape(PillCornerRadius),
            color = accentColor.copy(alpha = PillBackgroundAlpha),
            border = BorderStroke(PillBorderWidth, accentColor.copy(alpha = PillBorderAlpha)),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable(
                    enabled = onEventClick != null,
                    onClickLabel = "Open in calendar"
                ) {
                    onEventClick?.invoke(event)
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )

                Text(
                    text = "|",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = SeparatorAlpha)
                )

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (!location.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = accentColor.copy(alpha = LocationIconAlpha),
                        modifier = Modifier.size(LocationIconSize)
                    )
                    Text(
                        text = location,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }

        // Right gradient hairline rule
        Box(
            modifier = Modifier
                .widthIn(min = MinHairlineWidth)
                .weight(1f)
                .height(HairlineHeight)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(accentColor.copy(alpha = HairlineStartAlpha), Color.Transparent)
                    )
                )
        )
    }
}
