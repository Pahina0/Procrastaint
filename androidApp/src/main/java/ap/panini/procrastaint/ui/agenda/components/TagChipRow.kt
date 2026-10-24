package ap.panini.procrastaint.ui.agenda.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskTag

private const val SelectedContainerAlpha = 0.2f
private const val SelectedBorderAlpha = 0.7f

private val RowHorizontalPadding = 16.dp
private val RowVerticalPadding = 6.dp
private val ChipSpacing = 8.dp
private val ChipCornerRadius = 12.dp
private val ChipBorderWidth = 1.dp
private val ChipContentHorizontalPadding = 12.dp
private val ChipContentVerticalPadding = 8.dp
private val ChipContentSpacing = 6.dp
private val DotSize = 8.dp
private val AddIconSize = 16.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagChipRow(
    tags: List<TaskTag>,
    selectedTagId: Long?,
    onSelectTag: (Long?) -> Unit,
    onEditTag: (TaskTag) -> Unit,
    onAddTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = RowHorizontalPadding, vertical = RowVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(ChipSpacing)
    ) {
        item(key = "tag_filter_all") {
            AllFilterChip(
                isSelected = selectedTagId == null,
                onClick = { onSelectTag(null) }
            )
        }

        items(
            items = tags,
            key = { it.tagId }
        ) { tag ->
            val isSelected = selectedTagId == tag.tagId
            TagFilterChip(
                tag = tag,
                isSelected = isSelected,
                onClick = { onSelectTag(if (isSelected) null else tag.tagId) },
                onLongClick = { onEditTag(tag) }
            )
        }

        item(key = "tag_filter_add") {
            AddTagChip(
                onClick = onAddTag
            )
        }
    }
}

@Composable
private fun AllFilterChip(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(ChipCornerRadius))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(ChipCornerRadius),
        color = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        contentColor = if (isSelected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        border = BorderStroke(
            width = ChipBorderWidth,
            color = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = ChipContentHorizontalPadding,
                vertical = ChipContentVerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChipContentSpacing)
        ) {
            Text(
                text = "All",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TagFilterChip(
    tag: TaskTag,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = remember(tag.color) {
        tag.toRgbOrNull()?.let { Color(it.first, it.second, it.third) }
    } ?: MaterialTheme.colorScheme.primary

    val rawTitle = tag.displayTitle.ifBlank { tag.title }
    val displayLabel = if (rawTitle.startsWith('#')) rawTitle else "#$rawTitle"

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(ChipCornerRadius))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(ChipCornerRadius),
        color = if (isSelected) {
            tagColor.copy(alpha = SelectedContainerAlpha)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        contentColor = if (isSelected) {
            tagColor
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        border = BorderStroke(
            width = ChipBorderWidth,
            color = if (isSelected) {
                tagColor.copy(alpha = SelectedBorderAlpha)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = ChipContentHorizontalPadding,
                vertical = ChipContentVerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChipContentSpacing)
        ) {
            Box(
                modifier = Modifier
                    .size(DotSize)
                    .clip(CircleShape)
                    .background(tagColor)
            )
            Text(
                text = displayLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AddTagChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(ChipCornerRadius))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(ChipCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(
            width = ChipBorderWidth,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = ChipContentHorizontalPadding,
                vertical = ChipContentVerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChipContentSpacing)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add tag",
                modifier = Modifier.size(AddIconSize)
            )
            Text(
                text = "Tag",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
