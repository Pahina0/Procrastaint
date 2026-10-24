package ap.panini.procrastaint.ui.agenda.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val ChevronExpandedRotation = 180f
private const val ChevronCollapsedRotation = 0f
private const val ContentFadeInDurationMs = 200
private const val ContentFadeOutDurationMs = 150
private const val HeaderDividerAlpha = 0.5f

private val HeaderHorizontalPadding = 16.dp
private val HeaderVerticalPadding = 6.dp
private val TitleSpacing = 4.dp
private val TitlePaddingHorizontal = 8.dp
private val TitlePaddingVertical = 4.dp
private val TitlePillCornerRadius = 12.dp
private val NavButtonSpacing = 4.dp
private val NavButtonSize = 36.dp
private val HeaderBottomSpacing = 4.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalTime::class)
@Composable
fun CollapsibleCalendarHeader(
    focusedDate: Long,
    isMonthExpanded: Boolean,
    monthTasksByDate: Map<Long, List<TaskSingle>>,
    onSelectDate: (Long) -> Unit,
    onToggleMonthExpansion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeZone = TimeZone.currentSystemDefault()
    val focusedDateTime = remember(focusedDate, timeZone) {
        Instant.fromEpochMilliseconds(focusedDate).toLocalDateTime(timeZone)
    }

    val monthYearText = remember(focusedDateTime) {
        val monthName = focusedDateTime.month.name.lowercase().replaceFirstChar { it.titlecase() }
        "$monthName ${focusedDateTime.year}"
    }

    val firstDayOfMonth = remember(focusedDateTime.year, focusedDateTime.month) {
        LocalDate(focusedDateTime.year, focusedDateTime.month, 1)
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isMonthExpanded) ChevronExpandedRotation else ChevronCollapsedRotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ChevronRotation"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HeaderHorizontalPadding, vertical = HeaderVerticalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Interactive month title + animated chevron toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(TitlePillCornerRadius))
                        .clickable(onClick = onToggleMonthExpansion)
                        .padding(horizontal = TitlePaddingHorizontal, vertical = TitlePaddingVertical),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TitleSpacing)
                ) {
                    Text(
                        text = monthYearText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isMonthExpanded) "Collapse calendar" else "Expand calendar",
                        modifier = Modifier.rotate(chevronRotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Month navigation controls when expanded
                if (isMonthExpanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NavButtonSpacing)
                    ) {
                        IconButton(
                            onClick = {
                                val prevMonth = firstDayOfMonth.minus(1, DateTimeUnit.MONTH)
                                val prevTimestamp = LocalDateTime(
                                    year = prevMonth.year,
                                    month = prevMonth.month,
                                    day = 1,
                                    hour = 0,
                                    minute = 0,
                                    second = 0,
                                    nanosecond = 0
                                ).toInstant(timeZone).toEpochMilliseconds()
                                onSelectDate(prevTimestamp)
                            },
                            modifier = Modifier.size(NavButtonSize)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous month",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                val nextMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH)
                                val nextTimestamp = LocalDateTime(
                                    year = nextMonth.year,
                                    month = nextMonth.month,
                                    day = 1,
                                    hour = 0,
                                    minute = 0,
                                    second = 0,
                                    nanosecond = 0
                                ).toInstant(timeZone).toEpochMilliseconds()
                                onSelectDate(nextTimestamp)
                            },
                            modifier = Modifier.size(NavButtonSize)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next month",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            val enterTransition = fadeIn(animationSpec = tween(ContentFadeInDurationMs)) +
                expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            val exitTransition = fadeOut(animationSpec = tween(ContentFadeOutDurationMs)) +
                shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )

            AnimatedContent(
                targetState = isMonthExpanded,
                transitionSpec = { enterTransition togetherWith exitTransition },
                label = "CollapsibleCalendarAccordion"
            ) { expanded ->
                if (expanded) {
                    MonthCalendarGrid(
                        focusedDate = focusedDate,
                        monthTasksByDate = monthTasksByDate,
                        onSelectDate = { selectedTimestamp ->
                            onSelectDate(selectedTimestamp)
                            onToggleMonthExpansion()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    WeekStrip(
                        focusedDate = focusedDate,
                        onSelectDate = onSelectDate,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(HeaderBottomSpacing))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = HeaderDividerAlpha)
            )
        }
    }
}
