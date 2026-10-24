package ap.panini.procrastaint.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ap.panini.procrastaint.crash.GlobalExceptionHandler
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.ui.navigation.NavGraph
import ap.panini.procrastaint.ui.navigation.Route
import ap.panini.procrastaint.ui.navigation.isEntryIn
import ap.panini.procrastaint.ui.onboarding.OnBoardingScreen
import ap.panini.procrastaint.ui.theme.ProcrastaintTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val viewModel by viewModel<MainActivityViewModel>()
        val preferences by inject<PreferenceRepository>()

        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(this))

        if (intent?.getStringExtra("action") == "add_task") {
            viewModel.onShow()
        }

        setContent {
            ProcrastaintTheme {
                val onBoardComplete by preferences
                    .getBoolean(PreferenceRepository.ON_BOARDING_COMPLETE)
                    .collectAsStateWithLifecycle(initialValue = false)

                if (!onBoardComplete) {
                    OnBoardingScreen()
                } else {
                    MainContent(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val viewModel by viewModel<MainActivityViewModel>()
        if (intent.getStringExtra("action") == "add_task") {
            viewModel.onShow()
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun MainContent(viewModel: MainActivityViewModel) {
        val navController = rememberNavController()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val showFab = remember(currentDestination) {
            listOf(
                Route.Agenda::class,
                Route.Upcoming::class,
                Route.Calendar::class,
                Route.Library::class,
                Route.Tag::class
            ).isEntryIn(currentDestination)
        }

        val state by viewModel.uiState.collectAsStateWithLifecycle()
        Scaffold(
            floatingActionButton = {
                AnimatedVisibility(showFab) {
                    MediumFloatingActionButton(
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
                    viewModel::deleteEditTask,
                    viewModel::removeFutureRepeatsEditTask
                )
            }

            Box(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
                NavGraph(navController, Route.Agenda)
            }
        }
    }
}
