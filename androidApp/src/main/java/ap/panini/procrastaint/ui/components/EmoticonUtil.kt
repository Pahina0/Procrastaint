package ap.panini.procrastaint.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ap.panini.procrastaint.util.Emoticons

@Composable
fun rememberRandomEmoticon(): String = remember { Emoticons.random() }
