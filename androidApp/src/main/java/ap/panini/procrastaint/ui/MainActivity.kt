package ap.panini.procrastaint.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ap.panini.procrastaint.ui.calendar.CalendarTab
import ap.panini.procrastaint.ui.library.LibraryTab
import ap.panini.procrastaint.ui.settings.SettingsTab
import ap.panini.procrastaint.ui.theme.ProcrastaintTheme
import ap.panini.procrastaint.ui.upcoming.UpcomingTab
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabDisposable
import cafe.adriel.voyager.navigator.tab.TabNavigator
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private val tabs = listOf(
            LibraryTab(),
            UpcomingTab(),
            CalendarTab(),
            SettingsTab()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ProcrastaintTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TabNavigator(tab = tabs.first(),

                        tabDisposable = {
                            TabDisposable(
                                navigator = it,
                                tabs = tabs
                            )
                        }) { navigator ->
                        MainScreen(navigator = navigator)
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
            })
    }

    @Composable
    private fun NewTaskFAB(showTaskSheet: () -> Unit) {
        FloatingActionButton(onClick = showTaskSheet) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "New task")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen(navigator: TabNavigator) {
        var showTaskBottomSheet by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text(text = navigator.current.options.title) })
            },
            floatingActionButton = {
                NewTaskFAB() {
                    showTaskBottomSheet = true
                }
            },
            bottomBar = {
                NavigationBar {
                    tabs.forEach { TabNavigationItem(tab = it) }
                }
            },

            ) {
            Box(modifier = Modifier.padding(it)) {
                CurrentTab()
            }

            TaskBottomSheet(visible = showTaskBottomSheet) {
                showTaskBottomSheet = false
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TaskBottomSheet(visible: Boolean, onDismissRequest: () -> Unit) {
        val scope = rememberCoroutineScope()
        val state = rememberModalBottomSheetState()

        if (visible) {
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch {
                        state.hide()
                    }.invokeOnCompletion {
                        onDismissRequest()
                    }
                },
                sheetState = state
            ) {

                Text(text = """H" +
                        "" +
                        "" +
                        "" +
                        "" +
                        "" +
                        "" +
                        "" +
                        "" +
                        "" +
                        "a""")
            }
        }
    }
}




