package ap.panini.procrastaint.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ap.panini.procrastaint.R
import ap.panini.procrastaint.crash.GlobalExceptionHandler
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.ui.onboarding.OnBoardingScreen
import ap.panini.procrastaint.ui.theme.ProcrastaintTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val viewModel by viewModel<MainActivityViewModel>()
        val preferences by inject<PreferenceRepository>()

        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(this))

        setContent {
            ProcrastaintTheme {
                // determine which page to go to
                val onBoardComplete =
                    preferences.getBoolean(PreferenceRepository.ON_BOARDING_COMPLETE)
                        .collectAsStateWithLifecycle(
                            runBlocking {
                                preferences.getBoolean(PreferenceRepository.ON_BOARDING_COMPLETE)
                                    .first()
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

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val showBottomBar by remember(currentDestination) {
            mutableStateOf(
                BottomBarDestination.entries.map { it.direction::class }
                    .isEntryIn(currentDestination)
            )
        }

        val showFab by remember(currentDestination) {
            mutableStateOf(
                ( // all the bottom sheet stuff + tag, besides settings
                    BottomBarDestination.entries.map { it.direction::class } - Route.Settings::class +
                        Route.Tag::class
                    )
                    .isEntryIn(currentDestination)
            )
        }

        val state by viewModel.uiState.collectAsStateWithLifecycle()
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomBar(navController)
                }
            },

            floatingActionButton = {
                AnimatedVisibility(showFab) {
                    FloatingActionButton(
                        onClick = {
                            if (listOf(Route.Tag::class).isEntryIn(currentDestination)) {
                                val tagId = navBackStackEntry?.toRoute<Route.Tag>()?.tagId
                                viewModel.onShow(tagId = tagId)
                            } else {
                                viewModel.onShow()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = "New task")
                    }
                }
            },

        ) { padding ->
            if (state.visible) {
                TaskBottomSheet(
                    state,
                    viewModel::updateTask,
                    viewModel::updateDescription,
                    viewModel::viewNextParsed,
                    viewModel::onHide,
                    viewModel::getTagsStarting,
                    viewModel::save,
                    viewModel::deleteEditTask
                )
            }

            Box(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
                NavGraph(navController, Route.Calendar)
            }
        }
    }
}

private enum class BottomBarDestination(
    val direction: Route,
    val iconSelected: ImageVector,
    val iconDeselected: ImageVector,
    @StringRes val label: Int
) {
    Calendar(
        Route.Calendar,
        Icons.Default.CalendarMonth,
        Icons.Outlined.CalendarMonth,
        R.string.calendar
    ),
    Tasks(
        Route.Upcoming,
        Icons.Default.Upcoming,
        Icons.Outlined.Upcoming,
        R.string.upcoming
    ),

    Library(
        Route.Library,
        Icons.Default.LibraryAddCheck,
        Icons.Outlined.LibraryAddCheck,
        R.string.library
    ),

    Settings(
        Route.Settings,
        Icons.Default.Settings,
        Icons.Outlined.Settings,
        R.string.settings
    ),
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BottomBar(navController: NavController) {
    ShortNavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        BottomBarDestination.entries.forEach { topLevelRoute ->
            val isSelected by remember(currentDestination) {
                mutableStateOf(listOf(topLevelRoute.direction::class).isEntryIn(currentDestination))
            }

            ShortNavigationBarItem(
                icon = {
                    Icon(
                        if (isSelected) {
                            topLevelRoute.iconSelected
                        } else {
                            topLevelRoute.iconDeselected
                        },
                        contentDescription = stringResource(topLevelRoute.label)
                    )
                },
                label = { Text(stringResource(topLevelRoute.label)) },
                selected = isSelected,
                onClick = {
                    navController.navigate(topLevelRoute.direction) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
