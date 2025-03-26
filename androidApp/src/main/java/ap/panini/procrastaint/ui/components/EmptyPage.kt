package ap.panini.procrastaint.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Empty page used to display an icon and text (split by \n) to indicate an empty list
 *
 * @param icon
 * @param text text to display split by new lines if multiple lines
 * @param modifier modifier, usually including padding
 */
@Composable
fun EmptyPage(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    val splitText = remember { mutableListOf(*text.split("\n").toTypedArray()) }

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )

        for (sText in splitText) {
            Text(sText)
        }
    }
}