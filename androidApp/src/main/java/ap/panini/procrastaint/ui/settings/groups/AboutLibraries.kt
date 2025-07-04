package ap.panini.procrastaint.ui.settings.groups

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ap.panini.procrastaint.R
import ap.panini.procrastaint.ui.components.ScreenScaffold
import com.mikepenz.aboutlibraries.ui.compose.android.rememberLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination<RootGraph>
fun AboutLibrariesScreen(
    modifier: Modifier = Modifier,
    destinationsNavigator: DestinationsNavigator,
) {
    val libraries by rememberLibraries(R.raw.aboutlibraries)

    ScreenScaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Open Source Libraries") },
                navigationIcon = {
                    IconButton(onClick = { destinationsNavigator.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        LibrariesContainer(
            libraries,
            Modifier
                .fillMaxSize()
                .padding(it)
        )
    }
}
