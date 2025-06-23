package ap.panini.procrastaint.ui.settings.components.groups

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ap.panini.procrastaint.R
import com.mikepenz.aboutlibraries.ui.compose.android.rememberLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Composable
@Destination<RootGraph>
fun AboutLibrariesScreen(modifier: Modifier = Modifier) {
    val libraries by rememberLibraries(R.raw.aboutlibraries)

    Scaffold(modifier = modifier) {
        LibrariesContainer(
            libraries,
            Modifier
                .fillMaxSize()
                .padding(it)
        )
    }
}
