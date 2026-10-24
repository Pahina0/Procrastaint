package ap.panini.procrastaint.ui.settings.parser.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.ui.settings.parser.ComparisonDiffItem

private val DiffItemCornerRadius = 8.dp
private val BadgeCornerRadius = 4.dp
private val BadgePaddingHorizontal = 6.dp
private val BadgePaddingVertical = 2.dp
private val CardPadding = 16.dp
private val SpacingBetweenItems = 12.dp
private val SpacingSmall = 4.dp
private val SpacingMedium = 8.dp
private const val DividerAlpha = 0.5f

@Composable
fun ComparisonDiffCard(
    diffs: List<ComparisonDiffItem>,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(CardPadding),
            verticalArrangement = Arrangement.spacedBy(SpacingBetweenItems)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.CompareArrows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Before & After Comparison",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Visual changes compared to your previous settings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            diffs.forEachIndexed { index, item ->
                ComparisonDiffRow(item = item)
                if (index < diffs.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = DividerAlpha)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonDiffRow(
    item: ComparisonDiffItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            if (item.hasChanged) {
                Surface(
                    shape = RoundedCornerShape(BadgeCornerRadius),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "Changed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(
                            horizontal = BadgePaddingHorizontal,
                            vertical = BadgePaddingVertical
                        )
                    )
                }
            }
        }

        Text(
            text = "\"${item.input}\"",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Before",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.previousOutput,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val currentOutputModifier = if (item.hasChanged) {
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(DiffItemCornerRadius))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(SpacingSmall)
            } else {
                Modifier.weight(1f)
            }

            Column(modifier = currentOutputModifier) {
                Text(
                    text = "Current",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.hasChanged) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (item.hasChanged) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = item.currentOutput,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.hasChanged) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = if (item.hasChanged) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
