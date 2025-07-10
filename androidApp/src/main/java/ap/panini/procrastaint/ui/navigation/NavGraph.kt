package ap.panini.procrastaint.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ap.panini.procrastaint.ui.calendar.CalendarScreen
import ap.panini.procrastaint.ui.library.LibraryScreen
import ap.panini.procrastaint.ui.onboarding.OnBoardingScreen
import ap.panini.procrastaint.ui.settings.SettingsScreen
import ap.panini.procrastaint.ui.settings.groups.AboutLibrariesScreen
import ap.panini.procrastaint.ui.settings.sync.SyncScreen
import ap.panini.procrastaint.ui.tag.TagScreen
import ap.panini.procrastaint.ui.upcoming.UpcomingScreen

private const val AnimationSpeed = 300

@Composable
fun NavGraph(navController: NavHostController, startDestination: Route) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    AnimationSpeed, easing = LinearEasing
                )
            ) + slideIntoContainer(
                animationSpec = tween(AnimationSpeed, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(
                    AnimationSpeed, easing = LinearEasing
                )
            ) + slideOutOfContainer(
                animationSpec = tween(AnimationSpeed, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        }
    ) {
        composable<Route.OnBoarding> { OnBoardingScreen() }

        composable<Route.Calendar> { CalendarScreen() }

        composable<Route.Upcoming> { UpcomingScreen() }

        composable<Route.Library> {
            LibraryScreen(
                onNavigateToTag = { navController.navigate(it) }
            )
        }

        composable<Route.Tag> {
            TagScreen(
                args = it.toRoute(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Settings> {
            SettingsScreen(
                onNavigateToAboutLibraries = { navController.navigate(Route.AboutLibraries) },
                onNavigateToSync = { navController.navigate(Route.Sync) }
            )
        }

        composable<Route.AboutLibraries> {
            AboutLibrariesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Sync> {
            SyncScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
