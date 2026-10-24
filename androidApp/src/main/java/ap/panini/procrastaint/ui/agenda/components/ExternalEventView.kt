package ap.panini.procrastaint.ui.agenda.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent

/**
 * Renders an external calendar event with subordinate visual hierarchy.
 * Automatically delegates to [AgendaAllDayBanner] for all-day events and
 * [AgendaScheduleDivider] for timed events.
 */
@Composable
fun ExternalEventView(
    event: ExternalCalendarEvent,
    modifier: Modifier = Modifier,
    onEventClick: ((ExternalCalendarEvent) -> Unit)? = null
) {
    if (event.isAllDay) {
        AgendaAllDayBanner(
            event = event,
            modifier = modifier,
            onEventClick = onEventClick
        )
    } else {
        AgendaScheduleDivider(
            event = event,
            modifier = modifier,
            onEventClick = onEventClick
        )
    }
}
