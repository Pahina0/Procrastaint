package ap.panini.procrastaint.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.calendar.CalendarTab
import ap.panini.procrastaint.ui.inbox.InboxTab
import ap.panini.procrastaint.ui.library.LibraryTab
import ap.panini.procrastaint.ui.settings.SettingsTab
import ap.panini.procrastaint.ui.theme.ProcrastaintTheme
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabDisposable
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    companion object {
        private val tabs = listOf(
            LibraryTab(),
            InboxTab(),
            CalendarTab(),
            SettingsTab()
        )
    }

    private val screenModel: MainActivityScreenModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ProcrastaintTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TabNavigator(
                        tab = tabs.first(),

                        tabDisposable = {
                            TabDisposable(
                                navigator = it,
                                tabs = tabs
                            )
                        }
                    ) { navigator ->
                        MainScreen()
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.TabNavigationItem(tab: Tab) {
        val tabNavigator = LocalTabNavigator.current

        NavigationBarItem(
            selected = tabNavigator.current == tab,
            onClick = { tabNavigator.current = tab },
            icon = {
                Icon(painter = tab.options.icon!!, contentDescription = tab.options.title)
            }
        )
    }

    @Composable
    private fun NewTaskFAB(showTaskSheet: () -> Unit) {
        FloatingActionButton(onClick = showTaskSheet) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = "New task")
        }
    }

    @Composable
    private fun MainScreen() {
        var showTaskBottomSheet by remember { mutableStateOf(false) }

        Scaffold(
            floatingActionButton = {
                NewTaskFAB {
                    showTaskBottomSheet = true
                }
            },
            bottomBar = {
                NavigationBar {
                    tabs.forEach { TabNavigationItem(tab = it) }
                }
            },
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .consumeWindowInsets(it)
            ) {
                CurrentTab()

                if (showTaskBottomSheet) {
                    TaskBottomSheet(
                        state = screenModel.state.collectAsStateWithLifecycle().value,
                        updateTitle = screenModel::updateTask,
                        updateDescription = screenModel::updateDescription,
                        viewNextParsed = screenModel::viewNextParsed,
                        onDismissRequest = {
                            showTaskBottomSheet = false
                        },
                        screenModel::editManualStartTime,
                        screenModel::editEndTime,
                        screenModel::setRepeatTag,
                        screenModel::setRepeatInterval,
                        screenModel::save
                    )
                }
            }
        }
    }
}
