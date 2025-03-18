package ap.panini.procrastaint.ui.settings.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.data.entities.NetworkSyncItem
import ap.panini.procrastaint.util.toRFC3339
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel

@Destination<RootGraph>
@Composable
fun SyncScreen(
    navigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
    viewModel: SyncViewModel = koinViewModel(),
) {
    val syncItems = viewModel.syncList.collectAsStateWithLifecycle(listOf()).value


    Scaffold(modifier = modifier) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(syncItems) {
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
        }

        Text("Location: ${item.location}")
        Text("Action: ${item.action}")
        Text(item.time.toRFC3339(), style = MaterialTheme.typography.labelSmall)
    }
}