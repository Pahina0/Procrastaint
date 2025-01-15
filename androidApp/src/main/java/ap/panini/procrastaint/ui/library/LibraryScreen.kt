package ap.panini.procrastaint.ui.library

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier
) {
    Text("Library")
}
