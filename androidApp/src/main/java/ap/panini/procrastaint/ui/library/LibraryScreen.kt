package ap.panini.procrastaint.ui.library

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PregnantWoman
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ap.panini.procrastaint.ui.components.EmptyPage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier
) {
    EmptyPage(Icons.Default.PregnantWoman, "I told myself i would finish this this sem\nbut i got lazy")
}
