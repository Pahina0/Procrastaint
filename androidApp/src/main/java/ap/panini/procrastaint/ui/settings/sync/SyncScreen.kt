package ap.panini.procrastaint.ui.settings.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.data.entities.NetworkSyncItem
import ap.panini.procrastaint.ui.components.EmptyPage
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.util.toRFC3339
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun SyncScreen(
    modifier: Modifier = Modifier,
    viewModel: SyncViewModel = koinViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    ScreenScaffold(modifier = modifier.fillMaxSize()) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.sync() },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (state.syncList.isEmpty()) {
                EmptyPage(Icons.Default.SyncDisabled, "Nothing to sync!", Modifier.padding(padding))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.syncList) {
                    SyncItem(it)
                }
            }
        }
    }
}

@Composable
private fun SyncItem(item: NetworkSyncItem) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("TaskId: ${item.taskId}")
            Text("MetaId: ${item.metaId}")
            Text("CompletionId: ${item.completionId}")
            Text("Runs: ${item.failCount}")
        }

        Text("Location: ${item.location}")
        Text("Action: ${item.action}")
        Text(item.time.toRFC3339(), style = MaterialTheme.typography.labelSmall)
    }
}
