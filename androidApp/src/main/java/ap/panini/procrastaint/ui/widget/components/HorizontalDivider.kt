package ap.panini.procrastaint.ui.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height

@Composable
fun HorizontalDivider(height: Dp = 1.dp, modifier: GlanceModifier = GlanceModifier) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(GlanceTheme.colors.onSurface.getColor(context).copy(alpha = 0.12f))
    ) {}

}