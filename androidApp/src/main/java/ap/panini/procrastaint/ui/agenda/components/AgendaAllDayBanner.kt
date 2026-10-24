package ap.panini.procrastaint.ui.agenda.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.CalendarSource
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent

private const val BannerBackgroundAlpha = 0.08f
private const val BannerBorderAlpha = 0.20f
private const val BadgeBackgroundAlpha = 0.15f
private const val SourceIconAlpha = 0.7f
private val BannerCornerRadius = 8.dp
private val BadgeCornerRadius = 4.dp
private val BannerBorderWidth = 1.dp
private val SourceIconSize = 12.dp

@Composable
fun AgendaAllDayBanner(
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

    val sourceLabel = if (event.source == CalendarSource.GOOGLE) "Google" else "Device"
    val sourceIcon = if (event.source == CalendarSource.GOOGLE) Icons.Outlined.Public else Icons.Outlined.CalendarMonth

    Surface(
        shape = RoundedCornerShape(BannerCornerRadius),
        color = accentColor.copy(alpha = BannerBackgroundAlpha),
        border = BorderStroke(BannerBorderWidth, accentColor.copy(alpha = BannerBorderAlpha)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clickable(enabled = onEventClick != null) {
                onEventClick?.invoke(event)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(BadgeCornerRadius),
                color = accentColor.copy(alpha = BadgeBackgroundAlpha)
            ) {
                Text(
                    text = "ALL DAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Text(
                text = event.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = sourceIcon,
                contentDescription = null,
                tint = accentColor.copy(alpha = SourceIconAlpha),
                modifier = Modifier.size(SourceIconSize)
            )

            Text(
                text = sourceLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
