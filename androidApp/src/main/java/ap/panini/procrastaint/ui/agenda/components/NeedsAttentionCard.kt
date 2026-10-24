package ap.panini.procrastaint.ui.agenda.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.util.Date
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val CardContainerAlpha = 0.7f
private const val CardBorderAlpha = 0.4f
private const val DividerAlpha = 0.2f
private const val CheckboxUncheckedAlpha = 0.7f
private const val TagPillContainerAlpha = 0.15f
private const val OverdueAgeBulletAlpha = 0.4f
private const val MaxVisibleTags = 2
private const val MonthNameAbbreviationLength = 3

private val CardCornerRadius = 16.dp
private val CardBorderWidth = 1.5.dp
private val CardContentPadding = 16.dp
private val HeaderSpacing = 8.dp
private val HeaderIconSpacing = 8.dp
private val HeaderIconSize = 20.dp
private val ButtonHeight = 32.dp
private val ButtonHorizontalPadding = 12.dp
private val ButtonVerticalPadding = 4.dp
private val DividerThickness = 1.dp
private val TasksSpacing = 6.dp
private val TaskRowCornerRadius = 8.dp
private val TaskRowVerticalPadding = 4.dp
private val TaskRowHorizontalPadding = 4.dp
private val TaskContentSpacing = 8.dp
private val TaskTextSpacing = 2.dp
private val TaskTagSpacing = 6.dp
private val CheckboxSize = 36.dp
private val TagPillCornerRadius = 6.dp
private val TagPillHorizontalPadding = 6.dp
private val TagPillVerticalPadding = 2.dp

@Composable
fun NeedsAttentionCard(
    overdueTasks: List<TaskSingle>,
    onMoveAllToToday: () -> Unit,
    onCheckTask: (TaskSingle) -> Unit,
    onEditTask: (TaskSingle) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = overdueTasks.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CardCornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = CardContainerAlpha)
            ),
            border = BorderStroke(
                width = CardBorderWidth,
                color = MaterialTheme.colorScheme.error.copy(alpha = CardBorderAlpha)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CardContentPadding),
                verticalArrangement = Arrangement.spacedBy(HeaderSpacing)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(HeaderIconSpacing),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(HeaderIconSize)
                        )
                        Text(
                            text = "MISSED TASKS (${overdueTasks.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    Button(
                        onClick = onMoveAllToToday,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        contentPadding = PaddingValues(
                            horizontal = ButtonHorizontalPadding,
                            vertical = ButtonVerticalPadding
                        ),
                        modifier = Modifier.height(ButtonHeight)
                    ) {
                        Text(
                            text = "Move All to Today",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.error.copy(alpha = DividerAlpha),
                    thickness = DividerThickness
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(TasksSpacing)
                ) {
                    overdueTasks.forEach { task ->
                        OverdueTaskRow(
                            task = task,
                            onCheckTask = onCheckTask,
                            onEditTask = onEditTask
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverdueTaskRow(
    task: TaskSingle,
    onCheckTask: (TaskSingle) -> Unit,
    onEditTask: (TaskSingle) -> Unit,
    modifier: Modifier = Modifier
) {
    val overdueAge = remember(task.currentEventTime) {
        formatOverdueAge(task.currentEventTime)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TaskRowCornerRadius))
            .clickable { onEditTask(task) }
            .padding(
                horizontal = TaskRowHorizontalPadding,
                vertical = TaskRowVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TaskContentSpacing)
    ) {
        Checkbox(
            checked = false,
            onCheckedChange = { onCheckTask(task) },
            colors = CheckboxDefaults.colors(
                uncheckedColor = MaterialTheme.colorScheme.error.copy(alpha = CheckboxUncheckedAlpha),
                checkedColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.size(CheckboxSize)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(TaskTextSpacing)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TaskTagSpacing)
            ) {
                Text(
                    text = overdueAge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )

                task.tags.take(MaxVisibleTags).forEach { tag ->
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = OverdueAgeBulletAlpha)
                    )
                    OverdueTagPill(tag = tag)
                }
            }
        }
    }
}

@Composable
private fun OverdueTagPill(
    tag: TaskTag,
    modifier: Modifier = Modifier
) {
    val tagColor = remember(tag.color) {
        tag.toRgbOrNull()?.let { Color(it.first, it.second, it.third) }
    } ?: MaterialTheme.colorScheme.primary

    val rawTitle = tag.displayTitle.ifBlank { tag.title }
    val displayLabel = if (rawTitle.startsWith('#')) rawTitle else "#$rawTitle"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(TagPillCornerRadius))
            .background(tagColor.copy(alpha = TagPillContainerAlpha))
            .padding(
                horizontal = TagPillHorizontalPadding,
                vertical = TagPillVerticalPadding
            )
    ) {
        Text(
            text = displayLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = tagColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalTime::class)
internal fun formatOverdueAge(
    eventTimeMs: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    if (eventTimeMs <= 0L) return "Overdue"
    val eventDate = Instant.fromEpochMilliseconds(eventTimeMs)
        .toLocalDateTime(timeZone).date
    val today = Clock.System.now().toLocalDateTime(timeZone).date
    val daysAgo = eventDate.daysUntil(today)

    return when {
        daysAgo == 0 -> "Today"
        daysAgo == 1 -> "Yesterday"
        daysAgo > 1 && eventDate.year == today.year -> {
            val monthName = eventDate.month.name
                .lowercase()
                .replaceFirstChar { it.uppercase() }
                .take(MonthNameAbbreviationLength)
            if (Date.displayConfig.useEuropeanDate) {
                "${eventDate.day} $monthName"
            } else {
                "$monthName ${eventDate.day}"
            }
        }
        else -> {
            val monthName = eventDate.month.name
                .lowercase()
                .replaceFirstChar { it.uppercase() }
                .take(MonthNameAbbreviationLength)
            if (Date.displayConfig.useEuropeanDate) {
                "${eventDate.day} $monthName, ${eventDate.year}"
            } else {
                "$monthName ${eventDate.day}, ${eventDate.year}"
            }
        }
    }
}
