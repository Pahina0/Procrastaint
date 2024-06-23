package ap.panini.procrastaint.ui.inbox

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class InboxTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Inbox"
            val icon = rememberVectorPainter(Icons.Outlined.Upcoming)
            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<TaskListScreenModel>()
        val upcomingState by screenModel.state.collectAsStateWithLifecycle()
        val options by screenModel.optionsFlow.collectAsStateWithLifecycle(runBlocking { screenModel.optionsFlow.first() })

        UpcomingScreen(
            upcomingState = upcomingState,
            onCompleteTask = screenModel::checkTask,
            options = options,
            changeFilterOptions = screenModel::changeFilterOptions
        )
    }
}
