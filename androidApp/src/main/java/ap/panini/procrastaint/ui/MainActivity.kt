package ap.panini.procrastaint.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LibraryAddCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import ap.panini.procrastaint.R
import ap.panini.procrastaint.ui.theme.ProcrastaintTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.CalendarScreenDestination
import com.ramcosta.composedestinations.generated.destinations.LibraryScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.UpcomingScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.spec.TypedDestinationSpec
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.ramcosta.composedestinations.utils.startDestination
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ProcrastaintTheme {
                MainContent()
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainContent() {
        var showBottomSheet by remember { mutableStateOf(false) }
        val navController = rememberNavController()

        val scrollBehavior =
            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())


        val curDestination = navController.currentDestinationAsState().value
            ?: NavGraphs.root.startDestination


        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            bottomBar = {
                BottomBar(navController.rememberDestinationsNavigator(), curDestination)
            },

            topBar = {
                TopBar(curDestination)
            },

            floatingActionButton = {
                FloatingActionButton(onClick = {
                    showBottomSheet = true
                }) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "New task")
                }
            },

            ) {
            if (showBottomSheet) {
                TaskBottomSheet(
                    viewModel.uiState.collectAsStateWithLifecycle().value,
                    viewModel::updateTask,
                    viewModel::updateDescription,
                    viewModel::viewNextParsed,
                    onDismissRequest = { showBottomSheet = false },
                    viewModel::editManualStartTime,
                    viewModel::editEndTime,
                    viewModel::setRepeatTag,
                    viewModel::setRepeatInterval,
                    viewModel::save
                )
            }

            DestinationsNavHost(
                navGraph = NavGraphs.root,
                modifier = Modifier.padding(it),
                navController = navController
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TopBar(
        curDestination: TypedDestinationSpec<out Any?>,
        modifier: Modifier = Modifier,
    ) {
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = {
                Crossfade(
                    curDestination
                ) { state ->
                    Text(
                        BottomBarDestination.entries.firstOrNull {
                            it.direction == state
                        }?.let { stringResource(it.label) }
                            ?: "Unknown?",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
        )
    }

    @Composable
    private fun BottomBar(
        destinationsNavigator: DestinationsNavigator,
        curDestination: TypedDestinationSpec<out Any?>,
        modifier: Modifier = Modifier,
    ) {

        NavigationBar(modifier = modifier) {
            BottomBarDestination.entries.forEach { destination ->
                NavigationBarItem(
                    selected = curDestination == destination.direction,
                    onClick = {
                        destinationsNavigator.navigate(destination.direction) {
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(

                            if (curDestination == destination.direction) {
                                destination.iconSelected
                            } else {
                                destination.iconDeselected
                            }, contentDescription = stringResource(destination.label)
                        )
                    },
                    label = { Text(stringResource(destination.label)) },
                )
            }
        }
    }


    private enum class BottomBarDestination(
        val direction: DirectionDestinationSpec,
        val iconSelected: ImageVector,
        val iconDeselected: ImageVector,
        @StringRes val label: Int
    ) {
        Calendar(
            CalendarScreenDestination,
            Icons.Default.CalendarMonth,
            Icons.Outlined.CalendarMonth,
            R.string.calendar
        ),
        Tasks(
            UpcomingScreenDestination,
            Icons.Default.Upcoming,
            Icons.Outlined.Upcoming,
            R.string.upcoming
        ),
        Library(
            LibraryScreenDestination,
            Icons.Default.LibraryAddCheck,
            Icons.Outlined.LibraryAddCheck,
            R.string.library
        ),
        Settings(
            SettingsScreenDestination,
            Icons.Default.Settings,
            Icons.Outlined.Settings,
            R.string.settings
        ),
    }
}
