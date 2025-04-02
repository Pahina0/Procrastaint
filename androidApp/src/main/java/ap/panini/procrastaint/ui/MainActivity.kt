package ap.panini.procrastaint.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import ap.panini.procrastaint.R
import ap.panini.procrastaint.crash.GlobalExceptionHandler
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.ui.onboarding.OnBoardingScreen
import ap.panini.procrastaint.ui.theme.ProcrastaintTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.CalendarScreenDestination
import com.ramcosta.composedestinations.generated.destinations.LibraryScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.UpcomingScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.TypedDestinationSpec
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.ramcosta.composedestinations.utils.startDestination
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel by viewModel<MainActivityViewModel>()
        val preferences by inject<PreferenceRepository>()

        enableEdgeToEdge()

        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(this))

        setContent {
            ProcrastaintTheme {
                // determine which page to go to
                val onBoardComplete =
                    preferences.getBoolean(PreferenceRepository.ON_BOARDING_COMPLETE)
                        .collectAsStateWithLifecycle(
                            runBlocking {
                                preferences.getBoolean(PreferenceRepository.ON_BOARDING_COMPLETE).first()
                            }
                        ).value

                if (!onBoardComplete) {
                    OnBoardingScreen()
                } else {
                    MainContent(viewModel)
                }
            }
        }
    }

    @Composable
    private fun MainContent(viewModel: MainActivityViewModel) {
        val navController = rememberNavController()

        val curDestination = navController.currentDestinationAsState().value
            ?: NavGraphs.root.startDestination

        val state = viewModel.uiState.collectAsStateWithLifecycle().value
        Scaffold(
            bottomBar = {
                BottomBar(navController.rememberDestinationsNavigator(), curDestination)
            },

            floatingActionButton = {
                FloatingActionButton(onClick = {
                    viewModel.onShow()
                }) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "New task")
                }
            },

        ) {
            if (state.visible) {
                TaskBottomSheet(
                    state,
                    viewModel::updateTask,
                    viewModel::updateDescription,
                    viewModel::viewNextParsed,
                    viewModel::onHide,
                    viewModel::editManualStartTime,
                    viewModel::editEndTime,
                    viewModel::setRepeatTag,
                    viewModel::setRepeatInterval,
                    viewModel::save,
                    viewModel::deleteEditTask
                )
            }

            DestinationsNavHost(
                navGraph = NavGraphs.root,
                modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
                navController = navController
            )
        }
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
                            popUpTo(destination.direction) {
                                inclusive = true
                            }
                        }
                    },
                    icon = {
                        Icon(

                            if (curDestination == destination.direction) {
                                destination.iconSelected
                            } else {
                                destination.iconDeselected
                            },
                            contentDescription = stringResource(destination.label)
                        )
                    },
                    label = { Text(stringResource(destination.label)) },
                )
            }
        }
    }

    private enum class BottomBarDestination(
        val direction: Direction,
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
